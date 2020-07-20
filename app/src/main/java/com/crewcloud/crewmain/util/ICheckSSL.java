package com.crewcloud.crewmain.util;

import com.crewcloud.crewmain.datamodel.ErrorDto;

public interface ICheckSSL {
    void hasSSL(boolean hasSSL);
    void checkSSLError(ErrorDto errorData);
}

