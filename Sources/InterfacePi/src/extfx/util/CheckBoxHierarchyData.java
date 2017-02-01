package extfx.util;


/**
 * Extends {@link IHierarchyData} with the necessary methods for a TreeView with
 * CheckBox TreeCells.
 * 
 * @author eckig
 * @param <T>
 */
@SuppressWarnings("rawtypes")
public interface CheckBoxHierarchyData<T extends CheckBoxHierarchyData> extends HierarchyData<T>
{
    /**
     * @return {@code true} if the element is enabled otherwise {@code false}
     * @since 18.10.2013
     */
    public boolean isEnabled();

    /**
     * @param pEnabled
     *            {@code true} to enable the element or {@code false} to disable
     * @since 18.10.2013
     */
    public void setEnabled(boolean pEnabled);
}
