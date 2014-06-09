package com.nod.table.taskflows;

import javax.faces.event.ActionEvent;

import oracle.adf.view.rich.component.rich.output.RichOutputLabel;

public class TableLandingManagedBean {
    private RichOutputLabel tableNameOP;
    private RichOutputLabel tableIdOP;

    public TableLandingManagedBean() {
    }

    public void click(ActionEvent actionEvent) {
        System.out.println(tableIdOP.getValue() + ":" + tableNameOP.getValue());
    }

    public void setTableNameOP(RichOutputLabel tableNameOP) {
        this.tableNameOP = tableNameOP;
    }

    public RichOutputLabel getTableNameOP() {
        return tableNameOP;
    }

    public void setTableIdOP(RichOutputLabel tableIdOP) {
        this.tableIdOP = tableIdOP;
    }

    public RichOutputLabel getTableIdOP() {
        return tableIdOP;
    }
}
