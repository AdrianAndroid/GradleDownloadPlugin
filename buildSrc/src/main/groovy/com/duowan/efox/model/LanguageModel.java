package com.duowan.efox.model;

import java.io.Serializable;
import java.util.List;

public class LanguageModel implements Serializable {
    public int code;
    public String msg;
    public List<LanguageData> data;
}

class LanguageData implements Serializable {
    public List<PackagesData> langPackages;
}

class PackagesData implements Serializable {
    public String locale;
    public String url;
}