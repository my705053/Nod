package com.nod.utils;


import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import javax.servlet.http.HttpServletRequest;

import oracle.javatools.resourcebundle.BundleFactory;

import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;

/**
 * General useful static utilies for working with JSF.
 * NOTE: Updated to use JSF 1.2 ExpressionFactory.
 *
 * @author Duncan Mills
 * @author Steve Muench
 * @author Ric Smith
 *
 * $Id: JSFUtils.java 2383 2007-09-17 16:25:37Z drmills $
 */
public class JSFUtils {

    private static final String NO_RESOURCE_FOUND = "Missing resource: ";

    /**
     * Method for taking a reference to a JSF binding expression and returning
     * the matching object (or creating it).
     * @param expression EL expression
     * @return Managed object
     */
    public static Object resolveExpression(String expression) {
        FacesContext facesContext = getFacesContext();
        Application app = facesContext.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        ValueExpression valueExp =
            elFactory.createValueExpression(elContext, expression,
                                            Object.class);
        return valueExp.getValue(elContext);
    }

    public static String resolveRemoteUser() {
        FacesContext facesContext = getFacesContext();
        ExternalContext ectx = facesContext.getExternalContext();
        return ectx.getRemoteUser();
    }

    public static String resolveUserPrincipal() {
        FacesContext facesContext = getFacesContext();
        ExternalContext ectx = facesContext.getExternalContext();
        HttpServletRequest request = (HttpServletRequest)ectx.getRequest();
        return request.getUserPrincipal().getName();
    }

    public static Object resloveMethodExpression(String expression,
                                                 Class returnType,
                                                 Class[] argTypes,
                                                 Object[] argValues) {
        FacesContext facesContext = getFacesContext();
        Application app = facesContext.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        MethodExpression methodExpression =
            elFactory.createMethodExpression(elContext, expression, returnType,
                                             argTypes);
        return methodExpression.invoke(elContext, argValues);
    }

    /**
     * Method for taking a reference to a JSF binding expression and returning
     * the matching Boolean.
     * @param expression EL expression
     * @return Managed object
     */
    public static Boolean resolveExpressionAsBoolean(String expression) {
        return (Boolean)resolveExpression(expression);
    }

    /**
     * Method for taking a reference to a JSF binding expression and returning
     * the matching String (or creating it).
     * @param expression EL expression
     * @return Managed object
     */
    public static String resolveExpressionAsString(String expression) {
        return (String)resolveExpression(expression);
    }

    /**
     * Convenience method for resolving a reference to a managed bean by name
     * rather than by expression.
     * @param beanName name of managed bean
     * @return Managed object
     */
    public static Object getManagedBeanValue(String beanName) {
        StringBuffer buff = new StringBuffer("#{");
        buff.append(beanName);
        buff.append("}");
        return resolveExpression(buff.toString());
    }

    /**
     * Method for setting a new object into a JSF managed bean
     * Note: will fail silently if the supplied object does
     * not match the type of the managed bean.
     * @param expression EL expression
     * @param newValue new value to set
     */
    public static void setExpressionValue(String expression, Object newValue) {
        FacesContext facesContext = getFacesContext();
        Application app = facesContext.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        ValueExpression valueExp =
            elFactory.createValueExpression(elContext, expression,
                                            Object.class);

        //Check that the input newValue can be cast to the property type
        //expected by the managed bean.
        //If the managed Bean expects a primitive we rely on Auto-Unboxing
        //I could do a more comprehensive check and conversion from the object
        //to the equivilent primitive but life is too short
        Class bindClass = valueExp.getType(elContext);
        if (bindClass.isPrimitive() || bindClass.isInstance(newValue)) {
            valueExp.setValue(elContext, newValue);
        }
    }
    

    /**
     * Method for setting a new object into a JSF managed bean
     * Note: will fail silently if the supplied object does
     * not match the type of the managed bean.
     * @param expression EL expression
     * @param newValue new value to set
     */
    public static void setExpressionValue(String expression, Object newValue, boolean ignoreNewValueCheck) {
        FacesContext facesContext = getFacesContext();
        Application app = facesContext.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        ValueExpression valueExp = elFactory.createValueExpression(elContext, expression, Object.class);

        if (ignoreNewValueCheck) {
            valueExp.setValue(elContext, newValue);
        }
        else
        {
            //Check that the input newValue can be cast to the property type
            //expected by the managed bean.
            //If the managed Bean expects a primitive we rely on Auto-Unboxing
            //I could do a more comprehensive check and conversion from the object
            //to the equivilent primitive but life is too short
            Class bindClass = valueExp.getType(elContext);
            if (bindClass.isPrimitive() || bindClass.isInstance(newValue)) {
                valueExp.setValue(elContext, newValue);
            }
        }
    }
    
    /**
     *  Clear a JSF EL expression by setting it to null.
     * @param expression EL expression
     * @param newValue new value to set
     */
    public static void setExpressionValueToNull(String expression) {
        try 
        {
            FacesContext facesContext = getFacesContext();
            Application app = facesContext.getApplication();
            ExpressionFactory elFactory = app.getExpressionFactory();
            ELContext elContext = facesContext.getELContext();
            ValueExpression valueExp =
                elFactory.createValueExpression(elContext, expression,
                                                Object.class);
    
            //Check that the input newValue can be cast to the property type
            //expected by the managed bean.
            //If the managed Bean expects a primitive we rely on Auto-Unboxing
            //I could do a more comprehensive check and conversion from the object
            //to the equivilent primitive but life is too short
            Class bindClass = valueExp.getType(elContext);
            valueExp.setValue(elContext, null); 
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
    

    /**
     * Convenience method for setting the value of a managed bean by name
     * rather than by expression.
     * @param beanName name of managed bean
     * @param newValue new value to set
     */
    public static void setManagedBeanValue(String beanName, Object newValue) {
        StringBuffer buff = new StringBuffer("#{");
        buff.append(beanName);
        buff.append("}");
        setExpressionValue(buff.toString(), newValue);
    }


    /**
     * Convenience method for setting Session variables.
     * @param key object key
     * @param object value to store
     */
    public static

    void storeOnSession(String key, Object object) {
        FacesContext ctx = getFacesContext();
        Map sessionState = ctx.getExternalContext().getSessionMap();
        sessionState.put(key, object);
    }

    /**
     * Convenience method for getting Session variables.
     * @param key object key
     * @return session object for key
     */
    public static Object getFromSession(String key) {
        FacesContext ctx = getFacesContext();
        Map sessionState = ctx.getExternalContext().getSessionMap();
        return sessionState.get(key);
    }

    public static String getFromHeader(String key) {
        FacesContext ctx = getFacesContext();
        ExternalContext ectx = ctx.getExternalContext();
        return ectx.getRequestHeaderMap().get(key);
    }

    /**
     * Convenience method for getting Request variables.
     * @param key object key
     * @return session object for key
     */
    public static Object getFromRequest(String key) {
        FacesContext ctx = getFacesContext();
        Map sessionState = ctx.getExternalContext().getRequestMap();
        return sessionState.get(key);
    }
    /**
     * Pulls a String resource from the project specific property bundle
     * @param key string message key
     * @return Resource value or placeholder error String
     */
    public static String getStringFromBundle(String bundleName, String key) {
        ResourceBundle bundle = getBundle(bundleName);
        return getStringSafely(bundle, key, null);
    }

    /**
     * Pulls a String resource from the property bundle that
     * is defined under the application &lt;message-bundle&gt; element in
     * the faces config. Respects Locale
     * @param key string message key
     * @return Resource value or placeholder error String
     */
    public static String getStringFromBundle(String key) {
        ResourceBundle bundle = getBundle();
        return getStringSafely(bundle, key, null);
    }


    /**
     * Convenience method to construct a <code>FacesMesssage</code>
     * from a defined error key and severity
     * This assumes that the error keys follow the convention of
     * using <b>_detail</b> for the detailed part of the
     * message, otherwise the main message is returned for the
     * detail as well.
     * @param key for the error message in the resource bundle
     * @param severity severity of message
     * @return Faces Message object
     */
    public static FacesMessage getMessageFromBundle(String key,
                                                    FacesMessage.Severity severity) {
        ResourceBundle bundle = getBundle();
        String summary = getStringSafely(bundle, key, null);
        String detail = getStringSafely(bundle, key + "_detail", summary);
        FacesMessage message = new FacesMessage(summary, detail);
        message.setSeverity(severity);
        return message;
    }

    /**
     * Add JSF info message.
     * @param msg info message string
     */
    public static void addFacesInformationMessage(String msg) {
        FacesContext ctx = getFacesContext();
        FacesMessage fm =
            new FacesMessage(FacesMessage.SEVERITY_INFO, msg, "");
        ctx.addMessage(getRootViewComponentId(), fm);
    }

    /**
     * Add JSF error message.
     * @param msg error message string
     */
    public static void addFacesErrorMessage(String msg) {
        FacesContext ctx = getFacesContext();
        FacesMessage fm =
            new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, "");
        ctx.addMessage(getRootViewComponentId(), fm);
    }

    /**
     * Add JSF error message for a specific attribute.
     * @param attrName name of attribute
     * @param msg error message string
     */
    public static void addFacesErrorMessage(String attrName, String msg) {
        // TODO: Need a way to associate attribute specific messages
        //       with the UIComponent's Id! For now, just using the view id.
        //TODO: make this use the internal getMessageFromBundle?
        FacesContext ctx = getFacesContext();
        FacesMessage fm =
            new FacesMessage(FacesMessage.SEVERITY_ERROR, attrName, msg);
        ctx.addMessage(getRootViewComponentId(), fm);
    }
    /**
     * Add JSF warning message for a specific attribute.
     * @param compId 
     * @param attrName name of attribute
     * @param msg warning message string
     */
    public static void addFacesWarningMessage(String compId,String attrName, String msg) {
        // TODO: Need a way to associate attribute specific messages
        //       with the UIComponent's Id! For now, just using the view id.
        //TODO: make this use the internal getMessageFromBundle?
        FacesContext ctx = getFacesContext();
        FacesMessage fm =
            new FacesMessage(FacesMessage.SEVERITY_WARN, attrName, msg);
        if(compId == null || compId.equals("")){
            ctx.addMessage(getRootViewComponentId(), fm);
            return;
        }
        
        ctx.addMessage(compId, fm);
    }
    /**
     * Add JSF error message for a specific attribute.
     * @param attrName name of attribute
     * @param msg error message string
     */
    public static void addFacesErrorMessage(String compId,String attrName, String msg) {
        // TODO: Need a way to associate attribute specific messages
        //       with the UIComponent's Id! For now, just using the view id.
        //TODO: make this use the internal getMessageFromBundle?
        FacesContext ctx = getFacesContext();
        FacesMessage fm =
            new FacesMessage(FacesMessage.SEVERITY_ERROR, attrName, msg);
        ctx.addMessage(compId, fm);
    }

    // Informational getters

    /**
     * Get view id of the view root.
     * @return view id of the view root
     */
    public static String getRootViewId() {
        return getFacesContext().getViewRoot().getViewId();
    }

    /**
     * Get component id of the view root.
     * @return component id of the view root
     */
    public static String getRootViewComponentId() {
        return getFacesContext().getViewRoot().getId();
    }

    /**
     * Get FacesContext.
     * @return FacesContext
     */
    public static FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }
    
    private static ResourceBundle getBundle(String bundleName){
        return BundleFactory.getBundle(bundleName);
    }
    
    /*
   * Internal method to pull out the correct local
   * message bundle
   */

    private static ResourceBundle getBundle() {
        FacesContext ctx = getFacesContext();
        UIViewRoot uiRoot = ctx.getViewRoot();
        Locale locale = uiRoot.getLocale();
        ClassLoader ldr = Thread.currentThread().getContextClassLoader();
        return ResourceBundle.getBundle(ctx.getApplication().getMessageBundle(),
                                        locale, ldr);        
    }

    /**
     * Get an HTTP Request attribute.
     * @param name attribute name
     * @return attribute value
     */
    public static Object getRequestAttribute(String name) {
        return getFacesContext().getExternalContext().getRequestMap().get(name);
    }

    /**
     * Set an HTTP Request attribute.
     * @param name attribute name
     * @param value attribute value
     */
    public static void setRequestAttribute(String name, Object value) {
        getFacesContext().getExternalContext().getRequestMap().put(name,
                                                                   value);
    }

    /*
   * Internal method to proxy for resource keys that don't exist
   */

    public static String getStringSafely(ResourceBundle bundle, String key,
                                          String defaultValue) {
        String resource = null;
        try {
            resource = bundle.getString(key);
        } catch (MissingResourceException mrex) {
            if (defaultValue != null) {
                resource = defaultValue;
            } else {
                resource = NO_RESOURCE_FOUND + key;
            }
        }
        return resource;
    }

    /**
     * Locate an UIComponent in view root with its component id. Use a recursive way to achieve this.
     * Taken from http://www.jroller.com/page/mert?entry=how_to_find_a_uicomponent
     * @param id UIComponent id
     * @return UIComponent object
     */
    public static UIComponent findComponentInRoot(String id) {
        UIComponent component = null;
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null) {
            UIComponent root = facesContext.getViewRoot();
            component = findComponent(root, id);
        }
        return component;
    }

    /**
     * Locate an UIComponent from its root component.
     * Taken from http://www.jroller.com/page/mert?entry=how_to_find_a_uicomponent
     * @param base root Component (parent)
     * @param id UIComponent id
     * @return UIComponent object
     */
    public static UIComponent findComponent(UIComponent base, String id) {
        if (id.equals(base.getId()))
            return base;

        UIComponent children = null;
        UIComponent result = null;
        Iterator childrens = base.getFacetsAndChildren();
        while (childrens.hasNext() && (result == null)) {
            children = (UIComponent)childrens.next();
            if (id.equals(children.getId())) {
                result = children;
                break;
            }
            result = findComponent(children, id);
            if (result != null) {
                break;
            }
        }
        return result;
    }

    /**
     * Method to create a redirect URL. The assumption is that the JSF servlet mapping is
     * "faces", which is the default
     *
     * @param view the JSP or JSPX page to redirect to
     * @return a URL to redirect to
     */
    public static String getPageURL(String view) {
        FacesContext facesContext = getFacesContext();
        ExternalContext externalContext = facesContext.getExternalContext();
        String url =
            ((HttpServletRequest)externalContext.getRequest()).getRequestURL().toString();
        StringBuffer newUrlBuffer = new StringBuffer();
        newUrlBuffer.append(url.substring(0, url.lastIndexOf("faces/")));
        newUrlBuffer.append("faces");
        String targetPageUrl = view.startsWith("/") ? view : "/" + view;
        newUrlBuffer.append(targetPageUrl);
        return newUrlBuffer.toString();
    }

    /**
     * Method to add javascript to request.
     * @param script - script to be added to the page.
     */
    public static void addScript(StringBuffer script) {
        if(script != null) {
            ExtendedRenderKitService erks =
                Service.getRenderKitService(getFacesContext(), ExtendedRenderKitService.class);
            erks.addScript(getFacesContext(), script.toString());
        }
    }

    /**
     * Method to register key broad handlers (like CTRL S, CTRL F etc.,) to the current document object.
     * @param serverListener - default server listener to be called when user clicks on registered handlers.
     */
    public static void registerKeyBoardHandler(String serverListener) {
        StringBuffer script = new StringBuffer();        
        List<UIComponent> childComponents = getFacesContext().getViewRoot().getChildren();
        UIComponent parentComp = childComponents.get(0);
        if(parentComp != null) {
            String documentId = (parentComp).getClientId(getFacesContext());
            script.append("window.registerKeyBoardHandler('"+ serverListener +"','" +
                          documentId + "')");
            addScript(script);
        }
    }

}