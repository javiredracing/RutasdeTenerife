package tenerife.rutas.jfernandez.rutasdetenerife;

/**
 * Created by jfernandez on 22/05/2015.
 */
public class DrawerItem {
    private String name;
    private int iconId;
    private int id =0;

    /**
     * Set item
     * @param name Name of the item
     * @param iconId Image resource
     * @param id identifier
     */
    public DrawerItem(String name, int iconId, int id) {
        this.name = name;
        this.iconId = iconId;
        this.id = id;
    }

    public DrawerItem(String name, int iconId) {
        this.name = name;
        this.iconId = iconId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public void setId(int _id){
        this.id = _id;
    }

    public int getId(){
        return id;
    }
}
