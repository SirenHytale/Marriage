package plugin.siren.Compatibility;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import plugin.siren.Marriage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * The single boundary between Marriage and the Cultivation mod, so the Marriage
 * menu can show a couple their Partnered Cultivation (双修) standing.
 *
 * <p><b>Nothing else in this mod may reference a Cultivation class</b>, and this
 * class does not reference one either — every call below is reflective. That is
 * not squeamishness; it is forced by two hard constraints, and both are worth
 * writing down because the obvious implementation breaks servers.</p>
 *
 * <h2>Why there is no OptionalDependencies entry (this would break the server)</h2>
 *
 * <p>Cultivation's manifest already lists {@code Siren:Marriage} under
 * {@code OptionalDependencies}, because its Partnered Cultivation reads this
 * mod's API. Declaring {@code Siren:Cultivation} here in return would look
 * symmetrical and is in fact fatal: {@code Mod.calculateLoadOrder} turns every
 * optional dependency whose target is present into a real edge in the load-order
 * graph, and a two-way edge is a cycle. It throws
 * {@code ModLoadOrderException.Type.CYCLIC_DEPENDENCY} out of
 * {@code PluginManager.setup()} — so a server with BOTH mods installed would
 * fail to start. Never add Cultivation to this mod's manifest.</p>
 *
 * <h2>Why reflection still resolves without one</h2>
 *
 * <p>Because the class bridge is not gated on declared dependencies.
 * {@code PluginBridgeClassLoader.loadClass0} searches required dependencies
 * first, then optional ones, and then <em>falls through to every other loaded
 * plugin</em>. Declaring a dependency buys search priority and load ordering, not
 * access. So {@code Class.forName} from this mod's own classloader finds
 * Cultivation's classes at runtime with no manifest change at all.</p>
 *
 * <p>Reflection rather than a direct import for the matching reason: a direct
 * import needs Cultivation on this mod's Maven compile path, and Cultivation
 * already Maven-depends on Marriage — that is a build cycle as surely as the
 * other is a load cycle.</p>
 *
 * <h2>Resolved late, and never fatally</h2>
 *
 * <p>Everything is resolved on first use rather than in {@code setup()}. Without
 * a declared dependency there is no guarantee Cultivation has loaded by the time
 * Marriage's setup runs, whereas the first call here comes from a player opening
 * a menu — long after boot. Any failure at all, of any kind, latches this bridge
 * off for the session and is logged once; a compat display must never be able to
 * throw into a menu render or take a world thread down.</p>
 */
public final class CultivationBridge {

    /** The plugin identifier Cultivation registers under - {@code <Group>:<Name>}. */
    public static final String PLUGIN_ID = "Siren:Cultivation";

    private static final String API_CLASS = "plugin.siren.API.CultivationAPI";
    private static final String CONFIGS_CLASS = "plugin.siren.API.CultivationConfigs";
    private static final String PARTNER_MANAGER_CLASS = "plugin.siren.Utils.Partner.PartnerManager";
    /** The version lives on the plugin class, not the API facade. */
    private static final String PLUGIN_CLASS = "plugin.siren.Cultivation";

    /** Tri-state: null until first probed, then TRUE/FALSE for the session. */
    private static Boolean available;

    private static Method getRealm;
    private static Method isMeditating;
    private static Method partnerConfigs;
    private static Method getRequiredRealm;
    private static Method realmOrdinal;
    private static Method realmTranslationKey;
    private static Method cfgEnabled;
    private static Method cfgRadius;
    private static Method cfgMultiplier;
    private static Method cfgConvergence;
    private static Method apiVersion;

    private CultivationBridge(){}

    /**
     * Whether Cultivation is installed and its API resolved. Probed once, then
     * cached — a failed probe is never retried, so a broken pairing costs one
     * log line rather than one exception per menu render.
     */
    public static synchronized boolean isAvailable(){
        if(available != null){
            return available;
        }

        // The id is spelled out rather than read from a Cultivation constant, so
        // this check itself touches nothing of that mod.
        //
        // Deliberately NOT cached when it comes back null. Marriage declares no
        // dependency on Cultivation (see the class note - it would be a load
        // cycle), so nothing guarantees Cultivation has loaded when this is first
        // asked. Latching a "not installed" answer here would permanently disable
        // the page on a server that merely happened to probe a moment too early.
        // A map lookup is cheap enough to repeat; only a definite answer is kept.
        if(HytaleServer.get().getPluginManager().getPlugin(PluginIdentifier.fromString(PLUGIN_ID)) == null){
            return false;
        }

        // Throwable, not Exception: a failed link is an Error, and this must
        // degrade to "feature absent" rather than propagate.
        try{
            ClassLoader loader = CultivationBridge.class.getClassLoader();
            Class<?> api = Class.forName(API_CLASS, false, loader);
            Class<?> configs = Class.forName(CONFIGS_CLASS, false, loader);
            Class<?> partnerManager = Class.forName(PARTNER_MANAGER_CLASS, false, loader);

            getRealm = api.getMethod("getRealm",
                    com.hypixel.hytale.component.ComponentAccessor.class, Ref.class);
            isMeditating = api.getMethod("isMeditating",
                    com.hypixel.hytale.component.ComponentAccessor.class, Ref.class);
            apiVersion = Class.forName(PLUGIN_CLASS, false, loader).getMethod("getVersion");

            partnerConfigs = configs.getMethod("partner");
            getRequiredRealm = partnerManager.getMethod("getRequiredRealm");

            // The realm enum, reached through the method that returns it rather
            // than named directly - one less string to drift.
            Class<?> realmClass = getRealm.getReturnType();
            realmOrdinal = realmClass.getMethod("ordinal");
            realmTranslationKey = realmClass.getMethod("getTranslationKey");

            // PartnerConfig's own getters, off the Config wrapper's payload type.
            Object partnerConfig = configValue();
            Class<?> partnerConfigClass = partnerConfig.getClass();
            cfgEnabled = partnerConfigClass.getMethod("isPartneredCultivationEnabled");
            cfgRadius = partnerConfigClass.getMethod("getPartnerRadiusBlocks");
            cfgMultiplier = partnerConfigClass.getMethod("getPartnerQiMultiplier");
            cfgConvergence = partnerConfigClass.getMethod("isPartnerConvergenceEnabled");

            available = Boolean.TRUE;
            Marriage.LOGGER.atInfo().log("Cultivation " + apiVersion.invoke(null)
                    + " found - the Dual Cultivation page is available.");
        }catch(Throwable throwable){
            available = Boolean.FALSE;
            Marriage.LOGGER.atWarning().withCause(throwable).log(
                    "Cultivation is installed but its API could not be resolved - the Dual Cultivation page stays hidden. "
                            + "This is harmless; every other Marriage feature is unaffected.");
        }
        return available;
    }

    // === config reads ===

    /** Whether the server has Partnered Cultivation switched on. */
    public static boolean isPartneredCultivationEnabled(){
        Boolean value = call(cfgEnabled, Boolean.class);
        return value != null && value;
    }

    /** The Qi multiplier a partnered pair earns - the ×1.25 this page exists to show. */
    public static float getQiMultiplier(){
        Float value = call(cfgMultiplier, Float.class);
        return value == null ? 1f : value;
    }

    /** How close the two must sit for it to apply. */
    public static float getRadiusBlocks(){
        Float value = call(cfgRadius, Float.class);
        return value == null ? 0f : value;
    }

    /** Whether their Yin-Yang alignments drift toward each other while paired. */
    public static boolean isConvergenceEnabled(){
        Boolean value = call(cfgConvergence, Boolean.class);
        return value != null && value;
    }

    // === per-player reads ===

    /**
     * The translation key naming the realm Partnered Cultivation demands, or null
     * when the server sets no realm gate. The key belongs to Cultivation's own
     * lang files, which is exactly right - the realm should read in the player's
     * language, in Cultivation's wording.
     */
    @Nullable
    public static String getRequiredRealmKey(){
        if(!isAvailable()){
            return null;
        }
        try{
            Object realm = getRequiredRealm.invoke(null);
            return realm == null ? null : (String) realmTranslationKey.invoke(realm);
        }catch(Throwable throwable){
            return null;
        }
    }

    /**
     * Whether this player's own realm clears the gate.
     *
     * <p>Reads Cultivation's <em>effective</em> realm, so a server running a
     * replacement progression provider is judged on that ladder rather than on a
     * stored value it does not use.</p>
     */
    public static boolean meetsRealmRequirement(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref){
        if(!isAvailable()){
            return false;
        }
        try{
            Object required = getRequiredRealm.invoke(null);
            if(required == null){
                return true; // no gate configured
            }
            Object mine = getRealm.invoke(null, store, ref);
            if(mine == null){
                return false; // no CultivationComponent - not a cultivator
            }
            return (int) realmOrdinal.invoke(mine) >= (int) realmOrdinal.invoke(required);
        }catch(Throwable throwable){
            return false;
        }
    }

    /** Whether this player is meditating right now. */
    public static boolean isMeditating(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref){
        if(!isAvailable()){
            return false;
        }
        try{
            return (boolean) isMeditating.invoke(null, store, ref);
        }catch(Throwable throwable){
            return false;
        }
    }

    // === plumbing ===

    /** {@code CultivationConfigs.partner().get()} - the live PartnerConfig. */
    @Nullable
    private static Object configValue() throws Exception {
        Object config = partnerConfigs.invoke(null);
        return config instanceof Config<?> wrapper ? wrapper.get() : null;
    }

    /** Invokes a no-arg PartnerConfig getter, or returns null if anything is amiss. */
    @Nullable
    private static <T> T call(@Nullable Method method, @Nonnull Class<T> type){
        if(!isAvailable() || method == null){
            return null;
        }
        try{
            Object partnerConfig = configValue();
            return partnerConfig == null ? null : type.cast(method.invoke(partnerConfig));
        }catch(Throwable throwable){
            return null;
        }
    }
}
