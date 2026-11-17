package io.github.alexf6.garage.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.github.alexf6.garage.GarageMod;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Ajustes que solo importan al cliente, en {@code config/garage-client.json}: no dependen del servidor ni se
 * sincronizan. Se carga una sola vez al arrancar el cliente; el servidor dedicado nunca la lee.
 *
 * <p>Mismas reglas que {@link GarageConfig}: lo que falta toma el valor por defecto, lo que se sale de rango se
 * corrige y se avisa, y un archivo corrupto se guarda como {@code .bak} y se regenera.
 *
 * <p>Migración: {@code engineVolume} vivía en {@code garage.json}. Si este archivo todavía no existe, se crea con el
 * valor que haya allí, tanto al cargar esta config ({@link #load}) como al cargar la del servidor
 * ({@link LegacyConfigMigration}); así da igual cuál arranque primero. El cliente es el único que lee este
 * archivo, aunque un servidor dedicado que aún tenga la clave antigua lo crea una vez durante esa migración.
 *
 * @param engineVolume volumen del motor, de 0 (apagado) a 1
 * @param radioVolume  volumen de la radio de los vehículos, de 0 (apagado) a 1
 * @param radioRange   bloques a los que la radio deja de oírse, de {@link #MIN_RADIO_RANGE} a
 *                     {@link #MAX_RADIO_RANGE} (solo pistas mono)
 */
public record ClientConfig(float engineVolume, float radioVolume, int radioRange) {

    public static final int MIN_RADIO_RANGE = 1;
    public static final int MAX_RADIO_RANGE = 64;

    public static final int DEFAULT_RADIO_RANGE = 28;

    public static final ClientConfig DEFAULT = new ClientConfig(0.6F, 0.7F, DEFAULT_RADIO_RANGE);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Se escribe una sola vez al arrancar el cliente (load); el resto del tiempo solo se lee
    private static volatile ClientConfig current = DEFAULT;

    public static ClientConfig get() {
        return current;
    }

    /** Nombre del archivo dentro de {@code config/}. */
    public static final String FILE_NAME = GarageMod.MOD_ID + "-client.json";

    public static void load(Path file) {
        ClientConfig parsed = DEFAULT;
        try {
            if (!Files.exists(file)) {
                // Primer arranque: si garage.json aún trae el volumen antiguo, se parte de él (y no se pierde si
                // esta config se carga antes que la del servidor)
                Path legacyFile = file.resolveSibling(GarageConfig.FILE_NAME);
                Float legacy = LegacyConfigMigration.readLegacyEngineVolume(legacyFile);
                if (legacy != null) parsed = new ClientConfig(legacy, DEFAULT.radioVolume(), DEFAULT.radioRange());
            } else {
                try {
                    parsed = parse(JsonParser.parseString(Files.readString(file)).getAsJsonObject());
                } catch (JsonParseException | IllegalStateException | CharacterCodingException e) {
                    backUpAndRegenerate(file, e);
                    return;
                }
            }
        } catch (IOException e) {
            GarageMod.LOGGER.warn("No se pudo leer {}; se usan los valores por defecto: {}", file, e.toString());
            current = DEFAULT;
            return;
        }

        List<String> fixes = new ArrayList<>();
        current = sanitize(parsed, fixes);
        fixes.forEach(fix -> GarageMod.LOGGER.warn("{}: {}", FILE_NAME, fix));
        write(file, current);
    }

    /**
     * Crea {@code file} con {@code volume} (acotado al rango y avisando si hizo falta). No comprueba si ya existe:
     * de eso se ocupa {@link LegacyConfigMigration}.
     *
     * @return {@code true} si quedó escrito
     */
    static boolean create(Path file, float volume) {
        List<String> fixes = new ArrayList<>();
        ClientConfig config = sanitize(new ClientConfig(volume, DEFAULT.radioVolume(), DEFAULT.radioRange()), fixes);
        fixes.forEach(fix -> GarageMod.LOGGER.warn("{} (migrado de garage.json): {}", FILE_NAME, fix));
        return write(file, config);
    }

    /** Lee los volúmenes; falta o {@code null} = por defecto; cualquier tipo que no sea número es archivo dañado. */
    private static ClientConfig parse(JsonObject user) {
        float engine = parseVolume(user, "engineVolume", DEFAULT.engineVolume());
        float radio = parseVolume(user, "radioVolume", DEFAULT.radioVolume());
        int range = parseRange(user, "radioRange", DEFAULT.radioRange());
        return new ClientConfig(engine, radio, range);
    }

    private static int parseRange(JsonObject user, String key, int fallback) {
        JsonElement value = numberOrNull(user, key);
        if (value == null) return fallback;
        return (int) Math.clamp(Math.round(value.getAsDouble()), Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private static float parseVolume(JsonObject user, String key, float fallback) {
        JsonElement value = numberOrNull(user, key);
        return value == null ? fallback : value.getAsFloat();
    }

    /** Valor numérico de {@code key}, {@code null} si falta o es {@code null}; cualquier otro tipo es archivo dañado. */
    private static JsonElement numberOrNull(JsonObject user, String key) {
        JsonElement value = user.get(key);
        if (value == null || value.isJsonNull()) return null;
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new IllegalStateException(key + " debe ser un número, no " + value);
        }
        return value;
    }

    /** Corrige al rango válido y anota cada corrección en {@code fixes}. */
    static ClientConfig sanitize(ClientConfig config, List<String> fixes) {
        float engine = clampVolume("engineVolume", config.engineVolume(), DEFAULT.engineVolume(), fixes);
        float radio = clampVolume("radioVolume", config.radioVolume(), DEFAULT.radioVolume(), fixes);
        int range = Math.clamp(config.radioRange(), MIN_RADIO_RANGE, MAX_RADIO_RANGE);
        if (range != config.radioRange()) {
            fixes.add("radioRange = " + config.radioRange() + " está fuera de [" + MIN_RADIO_RANGE + ", "
                    + MAX_RADIO_RANGE + "]; se usa " + range);
        }
        return new ClientConfig(engine, radio, range);
    }

    private static float clampVolume(String key, float value, float fallback, List<String> fixes) {
        if (Float.isNaN(value)) {
            fixes.add(key + " no es un número; se usa " + fallback);
            return fallback;
        }
        float clamped = Math.clamp(value, 0.0F, 1.0F);
        if (clamped != value) {
            fixes.add(key + " = " + value + " está fuera de [0.0, 1.0]; se usa " + clamped);
        }
        return clamped;
    }

    private static void backUpAndRegenerate(Path file, Exception cause) {
        current = DEFAULT;
        if (ConfigFiles.backUpCorrupt(file, cause)) write(file, DEFAULT);
    }

    private static boolean write(Path file, ClientConfig config) {
        return ConfigFiles.write(file, GSON.toJson(config));
    }
}
