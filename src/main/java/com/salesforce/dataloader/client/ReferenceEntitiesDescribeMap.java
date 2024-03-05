/*
 * Copyright (c) 2015, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.salesforce.dataloader.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.salesforce.dataloader.dyna.ParentIdLookupFieldFormatter;
import com.salesforce.dataloader.dyna.ParentSObjectFormatter;
import com.salesforce.dataloader.exception.RelationshipFormatException;
import com.sforce.soap.partner.Field;

/**
 * 
 */
public class ReferenceEntitiesDescribeMap {

    private Map<String, DescribeRefObject> referenceEntitiesDescribeMap = new HashMap<String, DescribeRefObject>();
    private Map<String, ParentSObjectFormatter> relationshipFieldMap = new HashMap<String, ParentSObjectFormatter>();
    private static final Logger logger = LogManager.getLogger(ReferenceEntitiesDescribeMap.class);

    /**
     * 
     */
    public ReferenceEntitiesDescribeMap() {
        
    }
    
    public void put(String relationshipFieldName, DescribeRefObject parent, int numParentTypes) {
        ParentSObjectFormatter objField = null;
        try {
            objField = new ParentSObjectFormatter(parent.getParentObjectName(), relationshipFieldName, numParentTypes);
        } catch (RelationshipFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        referenceEntitiesDescribeMap.put(objField.toString(), parent);
        relationshipFieldMap.put(objField.toString(), objField);
    }
       
    public void clear() {
        this.referenceEntitiesDescribeMap.clear();
    }
    
    public int size() {
        return this.referenceEntitiesDescribeMap.size();
    }
    
    public Set<String> keySet() {
        return this.referenceEntitiesDescribeMap.keySet();
    }
    
    public ParentSObjectFormatter get(String relationshipFieldName) {
        ParentSObjectFormatter fieldFromInput = null;
        try {
            fieldFromInput = new ParentSObjectFormatter(relationshipFieldName, null);
        } catch (RelationshipFormatException e) {
            logger.error(e.getMessage());
            return null;
        }
        return this.relationshipFieldMap.get(fieldFromInput.toString());
    }
    // fieldName could be in the old format that assumes single parent: 
    // <relationship name attr of the field on child sobject>:<idlookup field name on parent sobject>
    //
    // fieldName could also be in the new format
    // <name of parent object>:<rel name on child object>.<idlookup field name on parent>
    public Field getParentField(String fieldName) {
        ParentIdLookupFieldFormatter lookupFieldStr = null;
        try {
            lookupFieldStr = new ParentIdLookupFieldFormatter(fieldName);
        } catch (RelationshipFormatException e) {
            logger.error(e.getMessage());
        }
        if (lookupFieldStr == null 
                || lookupFieldStr.getParentFieldName() == null 
                || lookupFieldStr.getParent().getRelationshipName() == null) {
            return null;
        } else {
            DescribeRefObject parent = getParentSObject(lookupFieldStr.getParent());
            if (parent == null) {
                return null;
            }
            for (Map.Entry<String, Field> refEntry : parent.getParentObjectFieldMap().entrySet()) {
                if (lookupFieldStr.getParentFieldName().equalsIgnoreCase(refEntry.getKey())) {
                    return refEntry.getValue();
                }
            }
            return null;
        }
    }
    
    // fieldName could be in the old format that assumes single parent: 
    // <relationship name attr of the field on child sobject>:<idlookup field name on parent sobject>
    //
    // fieldName could also be in the new format
    // <name of parent object>:<rel name on child object>.<idlookup field name on parent>

    public DescribeRefObject getParentSObject(String lookupFieldName) {
        try {
            return getParentSObject(new ParentSObjectFormatter(lookupFieldName, null));
        } catch (RelationshipFormatException e) {
            logger.error(e.getMessage());
        }
        return null;
    }
 
    private DescribeRefObject getParentSObject(ParentSObjectFormatter parentStr) {
        if (parentStr == null || parentStr.getRelationshipName() == null) {
            return null;
        }
        for (Map.Entry<String, DescribeRefObject> ent : referenceEntitiesDescribeMap.entrySet()) {
            if (parentStr.matches(ent.getKey())) {
                return ent.getValue();
            }
        }
        return null;
    }
}