package io.agritrack.philosofish.scale.diniargeo;

public class Commands {
    public static final String CMD_ALIM = "ALIM";
    public static final String CMD_CHECK_ALIBI_MEMORY = "ALRD00000-000000";
    public static final String CMD_CLEAR = "CLEAR";
    public static final String CMD_GTLD = "GTLD000101";
    public static final String CMD_PID = "PID";
    public static final String CMD_RALL = "REXT";
    public static final String CMD_READ = "READ";
    public static final String CMD_READ_VERSION = "VER";
    public static final String CMD_REXT = "REXT";
    public static final String CMD_TARE = "TARE";
    public static final String CMD_ZERO = "ZERO";

    public static final String CMD_TMAN(float f, int i) {
        return String.format("TMAN%06." + i + "f", Float.valueOf(f)).replace(",", ".");
    }
}