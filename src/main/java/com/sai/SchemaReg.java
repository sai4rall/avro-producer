package com.sai;

import org.apache.kafka.common.protocol.types.Schema;

import java.io.FileInputStream;

public class SchemaReg {
    public static void main(String[] args) throws Exception {
Producer producer=new Producer(args[0],args[2],"sai",args[1]);
producer.produce(args[3]);
    }
}
