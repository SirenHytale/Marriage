package plugin.siren.Utils;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import org.bson.BsonValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * A {@code Codec<Double>} that tolerates a blank or malformed number field instead
 * of taking the whole event decode down with it.
 *
 * <p>A client-side {@code NumberField} that has been cleared does not omit its key
 * or send null - it sends a quoted empty JSON string. {@code Codec.DOUBLE} has no
 * tolerance for that: a leading quote is not a digit or sign, so zero characters get
 * consumed and it throws. That failure happens while decoding the event payload,
 * before the page's own {@code handleDataEvent} runs, so it does not merely lose the
 * blank field - it discards <em>every</em> field in the same submission.</p>
 *
 * <p>This wraps the real {@link Codec#DOUBLE}: a leading quote is read as a string
 * and parsed by hand, returning null (treated as "not meaningfully provided", the
 * same as an absent field) when it is blank or unparseable. Anything else is
 * delegated straight through unchanged.</p>
 *
 * <p>Copied verbatim from the Mermaids mod (itself copied from Cultivation, where
 * this exact crash was diagnosed from a live report rather than predicted) for the
 * Marriage admin config editor's number fields.</p>
 */
public class LenientDoubleCodec implements Codec<Double> {
    public static final LenientDoubleCodec INSTANCE = new LenientDoubleCodec();

    private LenientDoubleCodec(){}

    @Override
    public Double decode(@Nonnull BsonValue bsonValue, ExtraInfo extraInfo){
        return Codec.DOUBLE.decode(bsonValue, extraInfo);
    }

    @Nonnull
    @Override
    public BsonValue encode(Double value, ExtraInfo extraInfo){
        return Codec.DOUBLE.encode(value, extraInfo);
    }

    @Nullable
    @Override
    public Double decodeJson(@Nonnull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
        if(!reader.peekFor('"')){
            return Codec.DOUBLE.decodeJson(reader, extraInfo);
        }

        String trimmed = reader.readString().trim();
        if(trimmed.isEmpty()){
            return null;
        }

        try{
            return Double.parseDouble(trimmed);
        }catch(NumberFormatException notANumber){
            return null;
        }
    }

    @Nonnull
    @Override
    public Schema toSchema(@Nonnull SchemaContext context){
        return Codec.DOUBLE.toSchema(context);
    }
}
