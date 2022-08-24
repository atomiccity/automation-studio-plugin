package city.atomic.automationstudio.unittest;

import java.util.List;

public class UnitTestServerResponse {
    public static class UnitTest {
        private String device;
        private String description;

        public String getDevice() {
            return device;
        }

        public void setDevice(String device) {
            this.device = device;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    private String version;
    private List<UnitTest> itemList;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<UnitTest> getItemList() {
        return itemList;
    }

    public void setItemList(List<UnitTest> itemList) {
        this.itemList = itemList;
    }
}
