/*
  * Copyright 2015 The CHOReVOLUTION project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package eu.chorevolution.validations.bpmn2choreographyvalidator.elementsvalidation;

import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.bpmn2.Choreography;
import org.eclipse.bpmn2.ChoreographyTask;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.MessageFlow;
import org.eclipse.bpmn2.Participant;
import org.eclipse.emf.ecore.impl.BasicEObjectImpl;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import eu.chorevolution.validations.bpmn2choreographyvalidator.Bpmn2ChoreographyValidator;
import eu.chorevolution.validations.bpmn2choreographyvalidator.Bpmn2ChoreographyValidatorException;
import eu.chorevolution.validations.bpmn2choreographyvalidator.Bpmn2ChoreographyValidatorMessage;
import eu.chorevolution.validations.bpmn2choreographyvalidator.Bpmn2ChoreographyValidatorResponse;

public class MessageTypeXSDValidation extends ElementsValidation {

	   /**
	    * This class validates that all types in a choreography message has a type defined:
	    * it can be defined in an XSD file or can be base types (boolean, int,..)
	 * 
	 */
	private final Bpmn2ChoreographyValidator bpmn2ChoreographyValidator;

	/**
	 * @param bpmn2ChoreographyValidator
	 */
	public MessageTypeXSDValidation(Bpmn2ChoreographyValidator bpmn2ChoreographyValidator) {
		this.bpmn2ChoreographyValidator = bpmn2ChoreographyValidator;
	}

	@Override	   
	   public Bpmn2ChoreographyValidatorResponse validateElements() throws Bpmn2ChoreographyValidatorException {
		   super.ValidatorResponse = new Bpmn2ChoreographyValidatorResponse();

		   List<String> baseTypes = Arrays.asList("anyURI", "boolean", "base64Binary", "hexBinary", "date", "dateTime", "time", "duration", "dayTimeDuration", "yearMonthDuration", "gDay", "gMonth", "gMonthDay", "gYear", "gYearMonth", "decimal", "integer", "nonPositiveInteger", "negativeInteger", "long", "int", "short", "byte", "nonNegativeInteger", "unsignedLong", "unsignedInt", "unsignedShort", "unsignedByte", "positiveInteger", "double", "float", "QName", "string", "normalizedString", "token", "language", "NMTOKEN", "Name", "NCName", "ID", "IDREF", "ENTITY", "untypedAtomic", "List", "Union");
		   
		   for (Choreography choreography : this.bpmn2ChoreographyValidator.getBpmnLoader().getChoreographies()) {
			   for(FlowElement flowEle : choreography.getFlowElements()) {
				   if(flowEle instanceof ChoreographyTask) {
					   ChoreographyTask chorTask = (ChoreographyTask) flowEle;
					   for(MessageFlow messageFlow : chorTask.getMessageFlowRef()) {

						   String messageStructureRef=null;

						   try {
							   //I have to use this function to get the Message Structure Ref (and not simply the name)
							   messageStructureRef=((BasicEObjectImpl)messageFlow.getMessageRef().getItemRef().getStructureRef()).eProxyURI().fragment();
						   }
						   catch(Exception e) {
							   String receiverParticipant=null;
							   for(Participant receiver : chorTask.getParticipantRefs()) {
								   if(receiver.getId()!=chorTask.getInitiatingParticipantRef().getId())
									   receiverParticipant=receiver.getName();
							   }
							   super.ValidatorResponse.addError(Bpmn2ChoreographyValidatorMessage.getMessage(
									   Bpmn2ChoreographyValidatorMessage.MESSAGE_TYPE_NOT_DEFINED_ERROR_MESSAGE, messageFlow.getMessageRef().getName(), choreography.getName(), chorTask.getName(), chorTask.getInitiatingParticipantRef().getName(), receiverParticipant));
							   
						   }
						   
						   Document xSDDocumentRoot=this.bpmn2ChoreographyValidator.getBpmnLoader().getXSDDocumentRoot();
					       XPath xPath = XPathFactory.newInstance().newXPath();
					       NodeList typeFound = null;

					       
					       try {
					    	   typeFound = (NodeList) xPath
					    			   .evaluate("/*/complexType[@name=\""+messageStructureRef+"\"]", xSDDocumentRoot.getDocumentElement(), XPathConstants.NODESET);
					    	   
					    	   if(typeFound.getLength()==0)
						    	   typeFound = (NodeList) xPath
				    			   .evaluate("/*/simpleType[@name=\""+messageStructureRef+"\"]", xSDDocumentRoot.getDocumentElement(), XPathConstants.NODESET);
				    	   

					       } catch (XPathExpressionException e) {
					    	   
					       }
							
					       //if the type is not present in xsd and is not an basetype
					       if((typeFound.getLength()==0)&&(!baseTypes.contains(messageStructureRef))) {
							   String receiverParticipant=null;
							   for(Participant receiver : chorTask.getParticipantRefs()) {
								   if(receiver.getId()!=chorTask.getInitiatingParticipantRef().getId())
									   receiverParticipant=receiver.getName();
							   }
					    	   super.ValidatorResponse.addError(Bpmn2ChoreographyValidatorMessage.getMessage(
					    			   Bpmn2ChoreographyValidatorMessage.MESSAGE_TYPE_NOT_PRESENT_IN_XSD_ERROR_MESSAGE, messageFlow.getMessageRef().getName(), choreography.getName(), chorTask.getName(), chorTask.getInitiatingParticipantRef().getName(), receiverParticipant, messageStructureRef));						   
					       }
						   
					   }
				   }
			   }
		   }

		
	      return super.ValidatorResponse;
	   }

	   @Override
	   public boolean getResponse() {
		   return super.ValidatorResponse.isValid();
	   }

	   @Override
	   public List<String> getErrors() {
		   return super.ValidatorResponse.getErrors();
	   }


	   
	   
	   
   }