package com.dassault_systemes.sfy.persistence;

import com.dassault_systemes.sfy.webservices.CreateStatement;
import java.util.Comparator;

class PLMPersistenceService$2 implements Comparator<CreateStatement> {
	PLMPersistenceService this$0;
	PLMPersistenceService$2(PLMPersistenceService var1) {
		this.this$0 = var1;
	}

	public int compare(CreateStatement var1, CreateStatement var2) {
		int var3 = 0;
		String var4 = (String) var1.getAttributes().get("instanceOf");
		String var5 = (String) var1.getAttributes().get("containedBy");
		String var6 = (String) var2.getAttributes().get("instanceOf");
		String var7 = (String) var2.getAttributes().get("containedBy");
		String var8 = var1.getId();
		String var9 = var2.getId();
		if (var4 != null && (var9.equals(var4) || var9.equals(var5))) {
			++var3;
		} else if (var6 != null && (var8.equals(var6) || var8.equals(var7))) {
			--var3;
		}

		return var3;
	}
}