package org.sadun.util.polling;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.sadun.util.BidirectionalComparator;
import org.sadun.util.ChainedComparator;

/**
 * A generic comparator that builds expressions on the file object properties.
 * 
 * @author Cristiano Sadun
 */

class GenericFileComparator implements Comparator {

    private static Map properties = new HashMap();
    private Comparator cc;
    private String specification;
    
    public GenericFileComparator(String specification) {
        this.specification=specification;
        StringTokenizer st = new StringTokenizer(specification,";");
        List comparators = new ArrayList();
        while (st.hasMoreTokens()) {
            String spec2=st.nextToken();
            comparators.add(new SingleExprComparator(spec2));
        }
        if (comparators.size()==1) cc=(Comparator)comparators.get(0);
        else {
            cc=(Comparator)comparators.get(comparators.size()-1);
            for(int i=comparators.size()-2;i>=0;i--) {
                Comparator cc1=(Comparator)comparators.get(i);
                cc=new ChainedComparator(cc1, cc);
            }
        }
    }
    
    
    private class SingleExprComparator extends BidirectionalComparator {
    
    
	    private Method methodToUse = null;
	    private Class returnType;
	
	    public SingleExprComparator(String spec) {
	        super(true);
	        parseSpec(spec);
	    }
	
	    /**
	     * @param spec
	     */
	    private void parseSpec(String spec) {
	        
            StringTokenizer st = new StringTokenizer(spec, " ");
	        while (st.hasMoreTokens()) {
	            String token = st.nextToken().trim().toLowerCase();
	            if ("ascending".equals(token))
	                ascending = true;
	            else if ("descending".equals(token))
	                ascending = false;
	            else {
	                // interpret as bean property on the File object
	                Method readMethod = (Method) properties.get(token);
	                if (readMethod == null)
	                    throw new IllegalArgumentException(
	                            "'"
	                                    + token
	                                    + "' is not a readable property for the File object. Available properties are: "+listProperties());
	                if (methodToUse != null)
	                    throw new IllegalArgumentException(
	                            "method to use specified twice:'"
	                                    + methodToUse.getName() + "' and '"
	                                    + readMethod.getName());
	                methodToUse = readMethod;
	                returnType = methodToUse.getReturnType();
	                checkSupportedType(methodToUse.getName(), returnType);
	            }
	        }
		        
	    }

	    /**
         * @return
         */
        private String listProperties() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            for(Iterator i = properties.keySet().iterator();i.hasNext();) {
                pw.print(i.next());
                if (i.hasNext()) pw.print(", ");
            }
            return sw.toString();
        }

        /**
	     * @param returnType2
	     */
	    private void checkSupportedType(String name, Class returnType) {
	        if (returnType == Boolean.TYPE || isNumeric(returnType)
	                || returnType == String.class)
	            return;
	        throw new IllegalArgumentException(
	                "Unsupported type for readable property '" + name + "': "
	                        + returnType.getName());
	    }
	
	    private boolean isNumeric(Class returnType) {
	        return returnType == Short.TYPE || returnType == Short.class
	                || returnType == Byte.TYPE || returnType == Byte.class
	                || returnType == Character.TYPE
	                || returnType == Character.class || returnType == Long.TYPE
	                || returnType == Long.class || returnType == Integer.TYPE
	                || returnType == Integer.class || returnType == Float.TYPE
	                || returnType == Float.class || returnType == Double.TYPE
	                || returnType == Double.class;
	    }
	    
	
	    /*
	     * (non-Javadoc)
	     * 
	     * @see org.sadun.util.polling.DirectoryPoller.BidirectionalComparator#getComparationValue(java.io.File,
	     *      java.io.File)
	     */
	    protected long getComparisonValue(File f1, File f2) {
	        try {
	            Object value1 = methodToUse.invoke(f1, new Object[0]);
	            Object value2 = methodToUse.invoke(f2, new Object[0]);
	            if (isNumeric(returnType)) {
	                // Apply numerical comparison
	                double l1=Double.valueOf(value1.toString()).doubleValue();
	                double l2=Double.valueOf(value2.toString()).doubleValue();
	                return new Double(l1-l2).longValue();
	            } else if (returnType == String.class) {
	                // Apply lexical comparison
	                String s1=(String)value1;
	                String s2=(String)value2;
	                return s2.compareTo(s1);
	            } else if (returnType == Boolean.class) {
	                int b1 = ((Boolean)value1).booleanValue() ? 1 :0;
	                int b2 = ((Boolean)value1).booleanValue() ? 1 :0;
	                return b1-b2;
	            } else throw new RuntimeException("Unsupported type "+returnType);
	        } catch (IllegalAccessException e) {
	            e.printStackTrace();
	            return 0L;
	        } catch (InvocationTargetException e) {
	            e.printStackTrace();
	            return 0L;
	        }
	    }
	    
	    public String toString() {
	        return "using file's "+methodToUse.getName()+" property ("+(ascending ? "ascending" : "descending")+")";
	    }
    }

    static {
        
        List toAvoid = Arrays.asList(
                new String[] { 
                        "class", 
                        "canonicalFile", 
                        "absolute", 
                        "file", 
                        "canonicalPath", 
                        "parentFile", 
                        "path", 
                        "absolutePath",
                        "parent",
                        "absoluteFile"
                });
        
        try {
            PropertyDescriptor[] descr = Introspector.getBeanInfo(File.class)
                    .getPropertyDescriptors();
            for (int i = 0; i < descr.length; i++) {
                if (descr[i].getReadMethod() != null &&
                        ! toAvoid.contains(descr[i].getName()))
                    properties
                            .put(descr[i].getName(), descr[i].getReadMethod());
            }
            try {
                properties.put("lastmodified", File.class.getMethod(
                        "lastModified", new Class[0]));
                Method mt =File.class.getMethod("length",
                        new Class[0]);
                properties.put("size", mt);
                properties.put("length", mt);
                
                mt =File.class.getMethod("canRead",
                        new Class[0]);
                properties.put("canRead", mt);
                
                mt =File.class.getMethod("canWrite",
                        new Class[0]);
                properties.put("canWrite", mt);
                
            } catch (NoSuchMethodException e1) {
                throw new Error(
                        "The current "+GenericFileComparator.class+"  is not compatible with the version of JDK in use",
                        e1);
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object o1, Object o2) {
        return cc.compare(o1,o2);
    }
    
    public String toString() {
        return cc.toString();
    }
    
    

    public String getSpecification() {
        return specification;
    }
}
