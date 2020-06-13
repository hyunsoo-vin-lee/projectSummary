package com.dassault_systemes.sfy.persistence;

import com.dassault_systemes.sfy.webservices.UpdateStatement;
import java.util.Comparator;

class PLMPersistenceService$1 implements Comparator<UpdateStatement> {
	PLMPersistenceService this$0;
	PLMPersistenceService$1(PLMPersistenceService var1) {
		this.this$0 = var1;
	}

	public int compare(UpdateStatement var1, UpdateStatement var2) {
		int var3 = 0;
		boolean var4 = "paths".equals(var1.getAttributeName())
				|| "FailureModesEffectsAnalysisAnalysisContext".equals(var1.getAttributeName());
		boolean var5 = "paths".equals(var2.getAttributeName())
				|| "FailureModesEffectsAnalysisAnalysisContext".equals(var2.getAttributeName());
		if (!var4 && !var5) {
			if (this.this$0.relationsToRep.containsKey(var1.getAttributeName())) {
				++var3;
			}

			if (this.this$0.relationsToRep.containsKey(var2.getAttributeName())) {
				--var3;
			}

			return var3;
		} else {
			if (var4) {
				++var3;
			}

			if (var5) {
				--var3;
			}

			return var3;
		}
	}
}