package dev.paperscript.legacy.runtime;

/** Parsed plugin.json of a legacy script. Mirrors the modern manifest. */
public final class ScriptManifest {
    public String name;
    public String version = "0.0.0";
    public String main = "index.js";
    public String apiVersion = "1";
    public String[] authors = new String[0];
}
