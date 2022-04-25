package mx.ori.pronouns;

import draylar.omegaconfig.api.Config;

public class PronounsConfig implements Config {
    public String pronouns = null;

    @Override
    public String getName() {
        return "pronouns";
    }
}
