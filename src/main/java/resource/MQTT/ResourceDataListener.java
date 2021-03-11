package resource.MQTT;

public interface ResourceDataListener<T> {

    public void onDataChanged(SmartObjectResource<T> resource, T updatedValue);

}