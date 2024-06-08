package com.example.demo;
import org.snmp4j.CommunityTarget;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.PDU;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.PDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class checkSwitchPorts {

    private static final String SNMP_COMMUNITY = "public";
    private static final String SNMP_IP_ADDRESS = "192.168.1.19";
    private static final int SNMP_PORT = 161;
    private static final String OID_BASE = "1.3.6.1.2.1.2.2.1.8"; // OID base para o status operacional das portas

    @Scheduled(fixedRate = 1000)
    public void checkSwitchPorts() {
        List<String> results = new ArrayList<>();
        try {
            TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
            transport.listen();

            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(SNMP_COMMUNITY));
            target.setAddress(new UdpAddress(SNMP_IP_ADDRESS + "/" + SNMP_PORT));
            target.setRetries(2);
            target.setTimeout(1500);
            target.setVersion(SnmpConstants.version2c);

            Snmp snmp = new Snmp(transport);
            // Supondo que você tenha 24 portas para verificar
//            for (int i = 1; i <= 24; i++) {
//                PDU pdu = new PDU();
//                OID portOID = new OID(OID_BASE + "." + i); // Cada porta tem seu próprio índice no OID
//                pdu.add(new VariableBinding(portOID));
//                pdu.setType(PDU.GET);
//                ResponseEvent event = snmp.get(pdu, target);
//                if (event != null && event.getResponse() != null) {
//                    int status = event.getResponse().get(0).getVariable().toInt();
//                    if (status == 1) { // 1 indica que a porta está "up"
//                        results.add("PORTA: " + i);
//                    }
//                }
//                System.out.println("Porta " + i + " está UP");
//            }
            OID[] columns = new OID[]{new OID( "1.3.6.1.2.1.2.2.1.8"  /*oid*/)};
            PDUFactory factory = new DefaultPDUFactory();
            TableUtils utils = new TableUtils(snmp, factory);
            List<TableEvent> events = utils.getTable(target, columns, null, null);


            for (TableEvent event : events) {
                if (event.isError()) {
                    System.err.println("Error: " + event.getErrorMessage());
                } else {
                    VariableBinding[] varBindings = event.getColumns();
                    if (varBindings != null) {
                        VariableBinding ifOperStatus = varBindings[0];
                        int portStatus = ifOperStatus.getVariable().toInt();
                        int portIndex = ifOperStatus.getOid().last();
                       // String portStatusText = (portStatus == 1) ? "UP" : "DOWN";
                        if(portStatus == 1 & portIndex <49 ) {
                            results.add("up: " + portIndex );
                        }


                    }
                }

            }


            System.out.println(results);
            snmp.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
