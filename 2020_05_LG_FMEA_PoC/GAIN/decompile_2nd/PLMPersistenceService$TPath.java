package com.dassault_systemes.sfy.persistence;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import matrix.db.Attribute;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.Relationship;
import matrix.db.RelationshipWithSelect;
import matrix.db.RelationshipWithSelectList;
import matrix.db.Path.Element;
import matrix.util.MatrixException;
import matrix.util.StringList;

public class PLMPersistenceService$TPath {
	private static final String APP_INDEX = "AppIndex";
	private static final String ID_REL = "IDRel";
	private static final String OUT_OF_SCOPES = "OutOfScopes";
	private static final String RELEVANT = "relevant";
	private static final String LOGICALID = "logicalid";
	private static final String MAJORID = "majorid";
	private static final String UPDATESTAMP = "updatestamp";
	private static final String PHYSICALID = "physicalid";
	private static final String TYPE = "type";
	protected String id;
	protected AttributeList attributes;
	protected String[] elements;
	protected String role;
	protected String semantic;
	PLMPersistenceService this$0;

	public PLMPersistenceService$TPath(PLMPersistenceService var1, String var2, String var3, String var4,
			AttributeList var5) {
		this.this$0 = var1;
		this.elements = null;
		this.role = null;
		this.semantic = null;
		this.id = var2;
		this.attributes = var5;
		this.role = var3;
		this.semantic = var4;
	}

	public void setElements(String var1, Map<String, String> var2) {
		if (var1 != null && var1.charAt(0) == '[' && var1.charAt(var1.length() - 1) == ']') {
			ArrayList var6 = new ArrayList();
			Matcher var4 = PLMPersistenceService.getWordPattern().matcher(var1);

			while (var4.find()) {
				String var5 = (String) var2.get(var4.group(1));
				var6.add(var5 != null ? var5 : var4.group(1));
			}

			this.elements = (String[]) var6.toArray(new String[0]);
		} else {
			String var3 = (String) var2.get(var1);
			this.elements = new String[]{var3 != null ? var3 : var1};
		}
	}

	public Element[] getPathElements(Context var1) throws MatrixException {
		StringList var2 = new StringList();
		var2.add("physicalid");
		var2.add("updatestamp");
		var2.add("majorid");
		var2.add("logicalid");
		var2.add("type");
		var2.add("relevant");
		Element[] var3 = null;
		RelationshipWithSelectList var4 = Relationship.getSelectRelationshipData(var1, this.elements, var2);
		BusinessObjectWithSelectList var5 = BusinessObject.getSelectBusinessObjectData(var1, this.elements, var2);
		var3 = new Element[this.elements.length];

		for (int var6 = 0; var6 < this.elements.length; ++var6) {
			RelationshipWithSelect var7 = (RelationshipWithSelect) var4.getElement(var6);
			BusinessObjectWithSelect var8 = (BusinessObjectWithSelect) var5.getElement(var6);
			boolean var9 = var7.getSelectData("physicalid") != null && !var7.getSelectData("physicalid").isEmpty();
			if (var9) {
				var3[var6] = new Element(var6 + 1, 2, var7.getSelectData("type"), var7.getSelectData("physicalid"),
						var7.getSelectData("majorid"), var7.getSelectData("logicalid"),
						var7.getSelectData("updatestamp"), true);
			} else {
				var3[var6] = new Element(var6 + 1, 1, var8.getSelectData("type"), var8.getSelectData("physicalid"),
						var8.getSelectData("majorid"), var8.getSelectData("logicalid"),
						var8.getSelectData("updatestamp"), true);
			}
		}

		return var3;
	}

	public AttributeList getAttributes() {
		String var1 = "0";

		for (Integer var2 = 1; var2 < this.elements.length; var2 = var2 + 1) {
			var1 = var1 + "|0";
		}

		this.attributes.add(new Attribute(new AttributeType("OutOfScopes"), var1));
		return this.attributes;
	}

	public String getId() {
		return this.id;
	}

	public String getRole() {
		return this.role;
	}

	public String getSemantic() {
		return this.semantic;
	}
}