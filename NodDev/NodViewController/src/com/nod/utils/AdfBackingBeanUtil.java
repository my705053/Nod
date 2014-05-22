package com.nod.utils;

import java.io.IOException;

import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewHandler;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import oracle.adf.controller.ControllerContext;
import oracle.adf.model.BindingContext;
import oracle.adf.model.DataControlFrame;
import oracle.adf.model.bean.DCDataRow;
import oracle.adf.model.binding.DCDataControl;
import oracle.adf.model.dcframe.TransactionProperties;
import oracle.adf.view.rich.component.fragment.UIXRegion;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.component.rich.data.RichTable;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.QueryEvent;
import oracle.adf.view.rich.model.CollectionModel;
import oracle.adf.view.rich.model.FilterableQueryDescriptor;

import oracle.adf.view.rich.model.RegionModel;

import oracle.binding.AttributeBinding;
import oracle.binding.BindingContainer;

import oracle.jbo.ApplicationModule;
import oracle.jbo.Transaction;
import oracle.jbo.ViewObject;
import oracle.jbo.uicli.binding.JUCtrlHierBinding;
import oracle.jbo.uicli.binding.JUCtrlHierNodeBinding;

import org.apache.myfaces.trinidad.component.UIXCollection;
import org.apache.myfaces.trinidad.component.UIXEditableValue;
import org.apache.myfaces.trinidad.component.UIXForm;
import org.apache.myfaces.trinidad.component.UIXSubform;
import org.apache.myfaces.trinidad.context.RequestContext;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;


/**
 * Providing a set of helper methods that can be used by backing beans.
 * The source is adapted from http://www.bekkerman.com/tiki/tiki-index.php?page=AdfBacking
 */

public class AdfBackingBeanUtil {
    
    public static void createSavePoint(String savePointId) {
        BindingContext bctx = BindingContext.getCurrent();
        DCDataControl dcDataControl = bctx.getDefaultDataControl();
        String sph = (String)dcDataControl.createSavepoint();
        RequestContext rctx = RequestContext.getCurrentInstance();
        rctx.getPageFlowScope().put(savePointId, sph);
    }

    public static void restoreSavePoint(String savePointId) {
        RequestContext rctx = RequestContext.getCurrentInstance();
        String sph = (String)rctx.getPageFlowScope().get(savePointId);
        BindingContext bctx = BindingContext.getCurrent();
        DCDataControl dcDataControl = bctx.getDefaultDataControl();
        dcDataControl.restoreSavepoint(sph);
    }

    public static void invokeTaskflowAction(String action) {
        NavigationHandler  nvHndlr = FacesContext.getCurrentInstance().getApplication().getNavigationHandler();
        nvHndlr.handleNavigation(FacesContext.getCurrentInstance(), null, action);
    }


    protected static void startTransaction() {
        BindingContext context = BindingContext.getCurrent();
        String dataControlFrameName = context.getCurrentDataControlFrame();
        DataControlFrame dcFrame = context.findDataControlFrame(dataControlFrameName);
        dcFrame.beginTransaction(new TransactionProperties());
    }

    public static ApplicationModule getApplicationModuleInstanceByName( String applicationModuleName)
    { 
        //A FacesContext instance is associated with a particular request at the beginning of request processing, by a call to the getFacesContext() method of the FacesContextFactory instance associated with the current web application
        FacesContext ctx = FacesContext.getCurrentInstance() ; //Getting Application Reference        
        ExpressionFactory elFactory = ctx.getApplication().getExpressionFactory();
        ELContext elContext = ctx.getELContext();  
        ValueExpression valueExp = elFactory.createValueExpression(elContext, "#{data." + applicationModuleName + "DataControl.dataProvider}",  Object.class);  
        return (ApplicationModule) valueExp.getValue(elContext); 
    }
    
    public static Transaction getTransactionContextForApplicationModule(String applicationModuleName) {
        return getApplicationModuleInstanceByName(applicationModuleName).getTransaction();
    }

    /**
     * Code is same as given in oracle.adfinternal.view.faces.taglib.listener.ResetActionListener
     * @param component
     * @return
     */
    public static UIComponent _getRootToReset(UIComponent component) {
        UIComponent previous = component;
        UIComponent parent = component.getParent();

        // Go up to the first parent which is a form, subform, or region
        while (parent != null) {
            if ((parent instanceof UIForm) || (parent instanceof UIXForm) ||
                (parent instanceof UIXSubform) ||
                (parent instanceof UIXRegion))
                return parent;

            previous = parent;
            parent = parent.getParent();
        }
        return previous;
    }


    public static void _resetChildren(UIComponent comp) {
        Iterator<UIComponent> kids = comp.getFacetsAndChildren();

        while (kids.hasNext()) {
            UIComponent kid = kids.next();

            if (kid instanceof UIXEditableValue) {
                ((UIXEditableValue)kid).resetValue();
                RequestContext.getCurrentInstance().addPartialTarget(kid);
            } else if (kid instanceof EditableValueHolder) {
                _resetEditableValueHolder((EditableValueHolder)kid);
                RequestContext.getCurrentInstance().addPartialTarget(kid);
            } else if (kid instanceof UIXCollection) {
                ((UIXCollection)kid).resetStampState();
                RequestContext.getCurrentInstance().addPartialTarget(kid);
            } else if (kid instanceof UIXRegion) {
                // Don't iterate inside regions
                continue;
            }

            _resetChildren(kid);
        }
    }

    private static void _resetEditableValueHolder(EditableValueHolder evh) {
        evh.setValue(null);
        evh.setSubmittedValue(null);
        evh.setLocalValueSet(false);
        evh.setValid(true);
    }


    public static Object getSelectedRow(RichTable table) {
        CollectionModel _tableModel = (CollectionModel)table.getValue();
        //the ADF object that implements the CollectionModel is
        //JUCtrlHierBinding. It is wrapped by the CollectionModel API
        JUCtrlHierBinding _adfTableBinding =
            (JUCtrlHierBinding)_tableModel.getWrappedData();
        //Acess the ADF iterator binding that is used with ADF table binding
        //    DCIteratorBinding _tableIteratorBinding = _adfTableBinding.getDCIteratorBinding();
        //the role of this method is to synchronize the table component
        //selection with the selection in the ADF model
        Object _selectedRowData = table.getSelectedRowData();
        //cast to JUCtrlHierNodeBinding, which is the ADF object
        //that represents a row
        JUCtrlHierNodeBinding _nodeBinding =
            (JUCtrlHierNodeBinding)_selectedRowData;

        if(_nodeBinding!=null) {
            return _nodeBinding.getRow();
        } else {
            return null;
        }
    }    public static Object getDataProvider(RichTable table) {
        DCDataRow dcrow = (DCDataRow)getSelectedRow(table);

        return dcrow.getDataProvider();
    }

    protected static Object getBindingAttributeValue(String binding) {
        BindingContainer bindings =
            BindingContext.getCurrent().getCurrentBindingsEntry();
        AttributeBinding ab =
            (AttributeBinding)bindings.getControlBinding(binding);
        return ab.getInputValue();
    }


    

    public static void displayErrorMessage(String messageText) {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage message = new FacesMessage(messageText);
        message.setSeverity(FacesMessage.SEVERITY_ERROR);
        context.addMessage(null, message);
    }

    /**
     * Find page component by its ID
     * @param componentId
     * @return
     */
    public UIComponent findComponent(String componentId) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIViewRoot root = facesContext.getViewRoot();
        return findComponent(root,componentId);
    }

    /**
     * Finds component with the given id
     */
    private static UIComponent findComponent(UIComponent c, String id) {
        if (id.equals(c.getId())) {
            return c;
        }
        Iterator<UIComponent> kids = c.getFacetsAndChildren();
        while (kids.hasNext()) {
            UIComponent found = findComponent(kids.next(), id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public static String getAttributeValue(UIComponent component, String attributeId) {
        String result = null;
        if (component != null) {
            Object attrValue = component.getAttributes().get(attributeId);
            result=(attrValue==null)?null:attrValue.toString();
        }
        return result;
    }

    public static void startPopup(RichPopup popup) {
        RichPopup.PopupHints ph = new RichPopup.PopupHints();
        popup.show(ph);
    }

    public static void clearTableFilters(RichTable myTable) {
        FilterableQueryDescriptor fqd =  
                (FilterableQueryDescriptor) myTable .getFilterModel();  
        if (fqd != null && fqd.getFilterCriteria() != null)  {  
            fqd.getFilterCriteria().clear();  
            myTable.queueEvent(new QueryEvent(myTable , fqd));
        }
    }
    
    /**
     * Sets focus on given component
     * @param uic UIComponent
     */
    public static void setFocusOn(UIComponent uic) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExtendedRenderKitService service =
               Service.getRenderKitService(facesContext,
                                                ExtendedRenderKitService.class);
        String componentId = uic.getClientId(FacesContext.getCurrentInstance());
        String script =
               "AdfPage.PAGE.findComponent('" + componentId + "').focus();";
                service.addScript(facesContext, script);
    }

    public static void launchNewBrowserWindow(String url, String name) {
        String script = "window.open('" + url + "','" + name + "')"; 
        ExtendedRenderKitService service = Service.getRenderKitService(FacesContext.getCurrentInstance(), ExtendedRenderKitService.class);
        service.addScript(FacesContext.getCurrentInstance(), script);
    }

   
    /**
     * refresh View Object.
     */
    public static void refreshVO(String appModuleName, String voId) {
        ApplicationModule am = AdfBackingBeanUtil.getApplicationModuleInstanceByName(appModuleName);
        if(am!=null) {
            ViewObject svo = am.findViewObject(voId);
            svo.clearCache();
            svo.reset();
            svo.executeQuery();
        }
    }
    
    // find a jsf component      
    public static UIComponent getUIComponent(String name) {  
     FacesContext facesCtx = FacesContext.getCurrentInstance();  
     return facesCtx.getViewRoot().findComponent(name) ;  
    }
    
    public static void refreshUIComponent(String name) {
        UIComponent componentToRefresh = getUIComponent(name);
        if (componentToRefresh != null) {
            AdfFacesContext.getCurrentInstance().addPartialTarget(componentToRefresh);
        }        
    }
    
    
    public static void refreshUIComponent(UIComponent componentToRefresh) {
        AdfFacesContext.getCurrentInstance().addPartialTarget(componentToRefresh);        
    }

    public static void refreshWholePage() {
        FacesContext facesCtx = FacesContext.getCurrentInstance();  
        String refreshPage = facesCtx.getViewRoot().getViewId(); 
        ViewHandler vh = facesCtx.getApplication().getViewHandler(); 
        UIViewRoot UVi = vh.createView(facesCtx,refreshPage); 
        UVi.setViewId(refreshPage); 
        facesCtx.setViewRoot(UVi);
    }
    
    public void redirectCurrentPage() {
        FacesContext fctx = FacesContext.getCurrentInstance();
        ExternalContext ectx = fctx.getExternalContext();
        
        String viewId = fctx.getViewRoot().getViewId();
        ControllerContext controllerCtx = null;
        controllerCtx = ControllerContext.getInstance();
        String activityURL = controllerCtx.getGlobalViewActivityURL(viewId);
        
        try{
            ectx.redirect(activityURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static Object evaluateEL(String anElExpression) {   
        FacesContext ctx = FacesContext.getCurrentInstance() ; //Getting Application Reference        
        ExpressionFactory elFactory = ctx.getApplication().getExpressionFactory();
        ELContext elContext = ctx.getELContext();  
        ValueExpression valueExp = elFactory.createValueExpression(elContext, anElExpression,  Object.class);  
        return valueExp.getValue(elContext); 

    }
    
    
    public static void refreshTaskflowRegion(String taskflowID) {
        RegionModel model = (RegionModel)AdfBackingBeanUtil.evaluateEL("#{bindings." + taskflowID + ".regionModel}");
        model.refresh(FacesContext.getCurrentInstance());  
    }
    
    
    /**
     * Find backing bean.
     * Pass in scope.beanClassname
     * Examples: backingBeanScope.myBacking1
     *           pageFlowScope.myBacking2
     *     MyBacking1 mb1 = (MyBacking1)findBackingBean("backingBeanScope.myBacking1");
     * @return
     */
    public static Object findBackingBean(String scopeAndName) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ELContext elctx = fc.getELContext();
        ExpressionFactory elFactory = fc.getApplication().getExpressionFactory();
        Object backingBean = 
            elFactory.createValueExpression(elctx, "#{"+scopeAndName+"}", 
                                            Object.class).getValue(elctx);
        return backingBean;
    }
    
}