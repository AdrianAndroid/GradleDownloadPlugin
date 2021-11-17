package com.duowan.efox.model;

import java.util.List;

public class LanguageModel {
    public int code;
    public String msg;
    public List<LanguageData> data;
}

class LanguageData {
    public List<PackagesData> langPackages;
}

class PackagesData {
    public String locale;
    public String url;
}