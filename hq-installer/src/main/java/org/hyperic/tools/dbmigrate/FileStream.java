package org.hyperic.tools.dbmigrate;

import java.io.IOException;

interface FileStream {
    
    void write(final Object o) throws IOException ; 
    void writeUTF(final String value) throws IOException ;
    
}//EOI 
