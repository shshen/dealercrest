package com.dealercrest.template;

enum TokenType {
    TAG_OPEN,        // <div
    TAG_CLOSE,       // </div>
    SELF_CLOSE,      // />
    TEXT,
    ATTRIBUTE_NAME,
    ATTRIBUTE_VALUE,
    EOF
}
