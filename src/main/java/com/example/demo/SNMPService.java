package com.example.demo;
import org.snmp4j.PDU;
import org.snmp4j.event.ResponseEvent;
import org.springframework.stereotype.Service;

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

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class SNMPService {

    // Método auxiliar para obter o sysUpTime
    private long getSysUpTime(Snmp snmp, CommunityTarget target) throws IOException {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.3.0"))); // OID for sysUpTime
        pdu.setType(PDU.GET);
        ResponseEvent event = snmp.send(pdu, target);
        if (event != null && event.getResponse() != null) {
            return event.getResponse().get(0).getVariable().toLong();
        }
        throw new IOException("Failed to get sysUpTime");
    }

    public List<String> getPortsUp(String address, String oid) throws IOException {
        List<String> results = new ArrayList<>();
        TransportMapping transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(GenericAddress.parse("udp:" + address + "/161"));
        target.setVersion(SnmpConstants.version2c);

        long sysUpTime = getSysUpTime(snmp, target); // Get the sysUpTime at the start

        PDUFactory factory = new DefaultPDUFactory();
        TableUtils utils = new TableUtils(snmp, factory);
        OID[] columns = new OID[]{new OID( "1.3.6.1.2.1.2.2.1.8"  /*oid*/)}; // OID for ifOperStatus
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
                    String portStatusText = (portStatus == 1) ? "UP" : "DOWN";
                    results.add("PORTA: " + portIndex + " " + portStatusText);

                    if (portStatus == 1) { // Only calculate last change time if the port is up
                        long lastChange = getLastChange(snmp, target, portIndex);
                        long timeSinceLastChange = sysUpTime - lastChange;
                        Instant now = Instant.now();
                        Instant lastChangeTime = now.minus(timeSinceLastChange * 10, ChronoUnit.MILLIS);
                        results.add("Last change time for PORTA " + portIndex + ": " + lastChangeTime);
                    }
                }
            }
        }

        snmp.close();
        return results;
    }

    // Método para obter o ifLastChange de uma porta específica
    private long getLastChange(Snmp snmp, CommunityTarget target, int portIndex) throws IOException {
        PDU pdu = new PDU();
        OID ifLastChangeOID = new OID("1.3.6.1.2.1.2.2.1.9." + portIndex);
        pdu.add(new VariableBinding(ifLastChangeOID));
        pdu.setType(PDU.GET);
        ResponseEvent event = snmp.send(pdu, target);
        if (event != null && event.getResponse() != null) {
            return event.getResponse().get(0).getVariable().toLong();
        }
        throw new IOException("Failed to get last change for port " + portIndex);
    }
}
