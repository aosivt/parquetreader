package ru.aosivt.rasterparquet.utils;

public enum FORMAT_QUERY {
    TYPE_FS() {
        @Override
        public String get(String[] parametersQuery) {
            return parametersQuery[FORMAT_QUERY.TYPE_FS.ordinal()];
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
    },
    OFFSET_COL() {
        @Override
        public Integer get(String[] parametersQuery) {
            if (parametersQuery.length > FORMAT_QUERY.OFFSET_COL.ordinal()) {
                return Integer.valueOf(parametersQuery[FORMAT_QUERY.OFFSET_COL.ordinal()]);
            } else {
                return 0;
            }
        }
    };

    public abstract Object get(String[] parameterQuery);
}
