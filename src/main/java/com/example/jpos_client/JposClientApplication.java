package com.example.jpos_client;

import org.jpos.iso.*;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.q2.Q2;
import org.jpos.q2.iso.QMUX;
import org.jpos.util.NameRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class JposClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(JposClientApplication.class, args);
	}

	@Bean
	public Q2 q2() {
		Q2 q2 = new Q2();
		q2.start();
		return q2;
	}

	@Bean
	public MUX mux (Q2 q2) throws NameRegistrar.NotFoundException {
		while (!q2.ready()) {
			ISOUtil.sleep(10);
		}
		return QMUX.getMUX("my-mux");
	}

	@Lazy
	@Autowired
	private MUX mux;

	@GetMapping("echo")
	public String echo() throws ISOException {
		ISOPackager packager = new GenericPackager("cfg/packager/iso87ascii-binary-bitmap.xml");

		ISOMsg msg = new ISOMsg();
		msg.setPackager(packager);

		msg.setMTI("0800");
		msg.set(11, "000001");
		msg.set(70, "301");

		byte[] packedMessage = msg.pack();
		System.out.println("Packed message: " + ISOUtil.hexString(packedMessage));

		ISOMsg respMsg = mux.request(msg, 30000);

		respMsg.setPackager(packager);
		System.out.println("Response message: ");
		for(int i = 0; i <= respMsg.getMaxField(); i++) {
			if (respMsg.hasField(i)) {
				System.out.println(i + ": " + respMsg.getString(i));
			}
		}

		return respMsg.toString();
	}

}
