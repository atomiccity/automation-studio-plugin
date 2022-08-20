package city.atomic.automationstudio;

public class Main {
    public static void main(String[] args) {
        Project p = Project.load("C:\\Users\\Micha\\Projects\\gardnerdenver-global_control_core-a30acc0841fe\\GD_GlobalController\\GD_GlobalController.apj");
        ConfigNew c = p.findConfig("GC_7in_mid2");
        Cpu cpu = c.getCpu();
        Hardware hw = c.getHardware();
        System.out.println(cpu.getAdditionalBuildOptions());
        System.out.println(cpu.getAnsicAdditionalBuildOptions());
        String opts = cpu.getAnsicAdditionalBuildOptions();
        System.out.println("Version: " + hw.getConfigVersion());
//        cpu.setAnsicAdditionalBuildOptions(opts + " -D MRT");
//        cpu.save();
    }
}
