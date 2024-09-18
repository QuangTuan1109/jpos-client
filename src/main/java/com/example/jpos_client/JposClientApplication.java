package com.example.jpos_client;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.MUX;
import org.jpos.q2.Q2;
import org.jpos.q2.iso.QMUX;
import org.jpos.tlv.GenericTagSequence;
import org.jpos.tlv.LiteralTagValue;
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
		ISOMsg msg = new ISOMsg();

		msg.setMTI("0800");
		msg.set(11, "000001");

		GenericTagSequence tagSequence = new GenericTagSequence();
		tagSequence.add(new LiteralTagValue("0012", "19960930000000"));
		tagSequence.add(new LiteralTagValue("0165", "M"));

		ISOMsg field48 = new ISOMsg(48);
		tagSequence.writeTo(field48);
		msg.set(field48);

		ISOMsg respMsg = mux.request(msg, 30000);
		return respMsg.toString();
	}

}
