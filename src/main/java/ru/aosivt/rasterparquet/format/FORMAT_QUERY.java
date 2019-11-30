package ru.aosivt.rasterparquet.format;

public enum FORMAT_QUERY {
    TYPE_FS() {
        @Override
        public String get(String[] parametersQuery) {
            return parametersQuery[FORMAT_QUERY.TYPE_FS.ordinal()];
        }
    },
    HOST() {
        @Override
        public String get(String[] parametersQuery) {
            return parametersQuery[FORMAT_QUERY.HOST.ordinal()];
        }
    },
    PATH() {
        @Override
        public String get(String[] parametersQuery) {
            return parametersQuery[FORMAT_QUERY.PATH.ordinal()];
        }
    },
    NAME_FILE() {
        @Override
        public String get(String[] parametersQuery) {
            return parametersQuery[FORMAT_QUERY.NAME_FILE.ordinal()];
        }
    };

    public abstract String get(String[] parameterQuery);
}
