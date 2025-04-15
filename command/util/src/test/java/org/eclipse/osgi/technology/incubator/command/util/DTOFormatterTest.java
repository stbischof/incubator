/**
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Stefan Bischof - initial
 */

package org.eclipse.osgi.technology.incubator.command.util;

import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.osgi.technology.incubator.command.util.dtoformatter.DTOFormatter;
import org.eclipse.osgi.technology.incubator.command.util.dtoformatter.ObjectFormatter;
import org.osgi.dto.DTO;
import org.osgi.framework.Bundle;
import org.osgi.framework.dto.BundleDTO;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.service.component.runtime.dto.ReferenceDTO;

public class DTOFormatterTest {
	DTOFormatter			formatter	= setup();
	ComponentDescriptionDTO	dto			= createComponentDescriptionDTO();

	public static class Foo extends DTO {
		public String field = "field";
	}
	@org.junit.jupiter.api.Test
	public void testListUnknown() {

		System.out.println(formatter.format(Arrays.asList(new Foo()), ObjectFormatter.INSPECT, (o, l, f) -> "" + o));
	}

	@org.junit.jupiter.api.Test
	public void testComponentDescriptionDTOPart() {

		System.out.println(formatter.format(dto, ObjectFormatter.PART, (o, l, f) -> "" + o));
	}

	@org.junit.jupiter.api.Test
	public void testComponentDescriptionDTOLine() {

		System.out.println(formatter.format(dto, ObjectFormatter.LINE, (o, l, f) -> "" + o));
	}

	@org.junit.jupiter.api.Test
	public void testComponentDescriptionDTOLines() {

		System.out.println(formatter.format(Arrays.asList(dto), ObjectFormatter.LINE, (o, l, f) -> "" + o));
	}

	public static class RawDTO extends DTO {
		public String	id		= "id";
		public long[]	values	= new long[] {
			1, 23, 4
		};
		public RawDTO[]	children;
	}

	@org.junit.jupiter.api.Test
	public void testRawDTO() {
		RawDTO parent = new RawDTO();
		RawDTO x = new RawDTO();
		x.children = new RawDTO[] {
			new RawDTO()
		};
		parent.children = new RawDTO[] {
			new RawDTO(), new RawDTO(), x
		};
		System.out.println(formatter.format(parent, ObjectFormatter.INSPECT, (o, l, f) -> "" + o));
	}

	@org.junit.jupiter.api.Test
	public void testComponentDescriptionDTOInspect() {

		System.out.println(formatter.format(dto, ObjectFormatter.INSPECT, (o, l, f) -> "" + o));
	}

	private DTOFormatter setup() {
		DTOFormatter formatter = new DTOFormatter();
		formatter.build(ComponentDescriptionDTO.class)
			.inspect()
			.fields("*")
			.line()
			.field("bundle")
			.field("activate")
			.field("modified")
			.field("deactivate")
			.field("configurationPolicy")
			.field("configurationPid")
			.field("references")
			.count()
			.part()
			.as(cdd -> String.format("%s[%s]", cdd.name, cdd.bundle.id));

		formatter.build(ReferenceDTO.class)
			.inspect()
			.fields("*")
			.line()
			.fields("*")
			.part()
			.field("name");

		formatter.build(BundleDTO.class)
			.inspect()
			.fields("*")
			.line()
			.field("id")
			.field("symbolicName")
			.field("version")
			.field("state")
			.part()
			.as(b -> String.format("%s[%s]", b.symbolicName, b.id));
		return formatter;
	}

	private ComponentDescriptionDTO createComponentDescriptionDTO() {
		ComponentDescriptionDTO dto = new ComponentDescriptionDTO();
		dto.name = "name";
		dto.bundle = new BundleDTO();
		dto.bundle.id = 10020;
		dto.bundle.lastModified = System.currentTimeMillis() - 10003232;
		dto.bundle.state = Bundle.ACTIVE;
		dto.bundle.symbolicName = "com.example.foo.bar";
		dto.bundle.version = "1.2.3";

		dto.factory = null;

		dto.scope = ServiceScope.BUNDLE.toString();
		dto.implementationClass = DTOFormatterTest.class.getName();
		dto.defaultEnabled = false;
		dto.immediate = true;
		dto.serviceInterfaces = new String[] {
			"com.example.Foo", "com.example.Bar"
		};
		dto.properties = new HashMap<>();
		dto.properties.put("foo", "bar");
		dto.references = new ReferenceDTO[2];
		dto.references[0] = new ReferenceDTO();
		dto.references[0].name = "r0";
		dto.references[0].cardinality = "1..1";
		dto.references[0].interfaceName = "com.example.Bar";
		dto.references[0].scope = ServiceScope.PROTOTYPE.toString();
		dto.references[1] = new ReferenceDTO();
		dto.references[1].name = "r1";
		dto.references[1].cardinality = "1..*";
		dto.references[1].interfaceName = "com.example.Foo";
		dto.references[1].scope = ServiceScope.BUNDLE.toString();
		dto.configurationPolicy = org.osgi.service.component.annotations.ConfigurationPolicy.IGNORE.toString();
		dto.configurationPid = new String[] {
			"com.example.configuration.pid.one", "com.example.configuration.pid.two"
		};
		return dto;
	}
}