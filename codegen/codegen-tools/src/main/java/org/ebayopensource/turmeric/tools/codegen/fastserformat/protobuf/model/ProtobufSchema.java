/**
 * 
 */
package org.ebayopensource.turmeric.tools.codegen.fastserformat.protobuf.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rkulandaivel
 *
 */
public class ProtobufSchema {
	
	private String m_dotprotoFileName = null;
	private String m_dotprotoTargetDir = null;
	private String m_jProtoOuterClassName = null;
	

	private String m_dotprotoFilePackage = null;
	private List<ProtobufImport> m_messagesImported = null;
	private List<ProtobufOption> m_dotprotoOptions = null;
	private List<ProtobufMessage> m_messages = null;

	private List<byte[]> m_metadataBytes = null;

	public String getDotprotoFileName() {
		return m_dotprotoFileName;
	}

	public void setDotprotoFileName(String dotprotoFileName) {
		this.m_dotprotoFileName = dotprotoFileName;
	}


	public String getDotprotoFilePackage() {
		return m_dotprotoFilePackage;
	}

	public void setDotprotoFilePackage(String dotprotoFilePackage) {
		this.m_dotprotoFilePackage = dotprotoFilePackage;
	}

	public List<ProtobufImport> getMessagesImported() {
		if(m_messagesImported == null){
			m_messagesImported = new ArrayList<ProtobufImport>();
		}
		return m_messagesImported;
	}

	public List<ProtobufOption> getDotprotoOptions() {
		if(m_dotprotoOptions == null){
			m_dotprotoOptions = new ArrayList<ProtobufOption>();
		}
		return m_dotprotoOptions;
	}


	public List<ProtobufMessage> getMessages() {
		if(m_messages == null){
			m_messages = new ArrayList<ProtobufMessage>();
		}
		return m_messages;
	}

	
	public String getDotprotoTargetDir() {
		return m_dotprotoTargetDir;
	}
	public void setDotprotoTargetDir(String dotprotoTargetDir) {
		this.m_dotprotoTargetDir = dotprotoTargetDir;
	}

	
	public byte[] getMetadataBytes() {
		if(m_metadataBytes.size() == 1){
			return m_metadataBytes.get(0);	
		}
		return new byte[ 0 ];
	}
	public void setMetadataBytes(byte[] metadataBytes) {
		this.m_metadataBytes = new ArrayList<byte[]>(1);
		this.m_metadataBytes.add( metadataBytes );
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ProtobufSchema [m_dotprotoFileName=" + m_dotprotoFileName
				+ ", m_dotprotoFilePackage=" + m_dotprotoFilePackage
				+ ", m_dotprotoOptions=" + m_dotprotoOptions + ", m_messages="
				+ m_messages + ", m_messagesImported=" + m_messagesImported
				+ "]";
	}
	public String getJProtoOuterClassName() {
		return m_jProtoOuterClassName;
	}
	public void setJProtoOuterClassName(String protoOuterClassName) {
		m_jProtoOuterClassName = protoOuterClassName;
	}

	
}
