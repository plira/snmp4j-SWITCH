package com.example.demo;

import org.snmp4j.CommunityTarget;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.PDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SNMPServicetst {

//    public List<String> getSnmpGet(String address, String oid) throws IOException {
//        List<String> results = new ArrayList<>();
//        TransportMapping transport = new DefaultUdpTransportMapping();
//
//
//        Snmp snmp = new Snmp(transport);
//        transport.listen();
//
//        CommunityTarget target = new CommunityTarget();
//        target.setCommunity(new OctetString("myReadCommunity"));
//        target.setAddress(new UdpAddress(address + "/161"));
//        target.setVersion(SnmpConstants.version2c);
//
//        PDU pdu = new PDU();
//        pdu.add(new VariableBinding(new OID(oid)));
//        pdu.setType(PDU.GET);
//
//        ResponseEvent event = snmp.send(pdu, target);
//        Vector<VariableBinding> variableBindings = null;
//        if (event != null && event.getResponse() != null) {
//            variableBindings = (Vector<VariableBinding>) event.getResponse().getVariableBindings();
//        }
//        if (variableBindings != null) {
//            for (VariableBinding vb : variableBindings) {
//                results.add(vb.getOid() + " = " + vb.getVariable());
//            }
//        }
//
//        snmp.close();
//        return results;
//    }

    public List<String> getPortsUp(String address, String oid) throws IOException {
        List<String> results = new ArrayList<>();
        TransportMapping transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(GenericAddress.parse("udp:" + address + "/161"));
        target.setVersion(SnmpConstants.version2c);

        PDUFactory factory = new DefaultPDUFactory();
        TableUtils utils = new TableUtils(snmp, factory);
        //1.3.6.1.2.1.1.3.0  1.3.6.1.2.1.2.2.1.8
        OID[] columns = new OID[]{new OID(oid)}; // ifOperStatus
        List<TableEvent> events = utils.getTable(target, columns, null, null);

        for (TableEvent event : events) {
            if (event.isError()) {
                System.err.println("Error: " + event.getErrorMessage());
            } else {
                VariableBinding[] varBindings = event.getColumns();
                if (varBindings != null) {
                    VariableBinding ifOperStatus = varBindings[0];
                    if (ifOperStatus.getVariable().toInt() == 1) { // Checking if the interface is up
//                        results.add(ifOperStatus.getOid().toString() + " = " + ifOperStatus.getVariable());
//                        if(ifOperStatus.getOid().last()<49) {

                            results.add("PORTA: " + ifOperStatus.getOid().last() + " UP");
//                        }

                    } else {

                        results.add("PORTA: " + ifOperStatus.getOid().last() + " DOWN");
                    }
                }
            }
        }

        snmp.close();
        return results;
    }
}
