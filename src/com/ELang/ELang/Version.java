package com.ELang.ELang;

public class Version {
    public int major;
    public int minor;
    public int patch;

    Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    @Override
    public String toString() {
        return "".concat(Integer.toString(this.major)).concat(".").concat(Integer.toString(this.minor)).concat(".").concat(Integer.toString(this.patch));
    }
}