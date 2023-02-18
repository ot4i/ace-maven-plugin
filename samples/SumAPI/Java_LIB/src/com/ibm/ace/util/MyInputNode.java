package com.ibm.ace.util;

import com.ibm.broker.plugin.*;
import com.ibm.broker.javacompute.*;

public class MyInputNode extends MbInputNode implements MbInputNodeInterface
{
  private String _attribute1;

  public MyInputNode() throws MbException
  {
    // create terminals here
    createOutputTerminal("out");
    createOutputTerminal("failure");
    createOutputTerminal("catch");
  }
  
  public int run(MbMessageAssembly assembly) throws MbException
  {
	  // dummy - no implementation - for compiling only 
	  MbJavaComputeNode myComputNode = new MbJavaComputeNode() {
		
		@Override
		public void evaluate(MbMessageAssembly arg0) throws MbException {
			// TODO Auto-generated method stub
			
		}
	};
	  return 0;
  
  }

  // the following static method declares the name of this node to the broker
  public static String getNodeName()
  {
    return "dummyValue";
  }
 
  // by supplying the following two methods, the broker infers this node
  // has an attribute named 'firstAttribute'.
  public String getFirstAttribute()
  {
    return _attribute1;
  }

  public void setFirstAttribute(String attr)
  {
    _attribute1 = attr;
  }



  public void onDelete( )
  {
    // perform node clean up if necessary.  
  }
}