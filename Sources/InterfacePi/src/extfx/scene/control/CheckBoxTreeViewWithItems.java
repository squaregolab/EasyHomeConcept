package extfx.scene.control;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import extfx.util.CheckBoxHierarchyData;

/**
 * {@link TreeViewWithItems} rendering each item as a {@link CheckBoxTreeItem}
 * making use of {@link CheckBoxHierarchyData#isEnabled()} and
 * {@link CheckBoxHierarchyData#setEnabled(boolean)}.
 *
 * @author eckig
 * @param <T>
 *            type of items contained in TreeView
 * @since 31.10.2013
 */
public class CheckBoxTreeViewWithItems<T extends CheckBoxHierarchyData<T>> extends TreeViewWithItems<T> {
	
    /**
     * Constructor, creating an empty root node
     *
     * @since 31.10.2013
     */
    public CheckBoxTreeViewWithItems() {
        super(new CheckBoxTreeItem<T>());
    }

    @Override
    protected TreeItem<T> createTreeItem(final T pValue) {
        CheckBoxTreeItem<T> item = new CheckBoxTreeItem<T>();
        item.setValue(pValue);
        item.setExpanded(true);
        if (pValue != null) {
            item.selectedProperty().set(pValue.isEnabled());
            item.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> pArg0, Boolean pArg1, Boolean pArg2)
                {
                    if (pArg2 != null && !pArg2.equals(pArg1)) {
                        pValue.setEnabled(pArg2.booleanValue());
                    }
                }
            });
        }
        return item;
    }
}
