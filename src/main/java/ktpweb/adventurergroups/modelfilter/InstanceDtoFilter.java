package ktpweb.adventurergroups.modelfilter;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class InstanceDtoFilter
{

    private static String[] simpleFilter = { "id", "active", "subdomainName",
            "displayName" };

    private static String[] fullFilter = { "id", "active", "subdomainName",
            "displayName", "description", "createDate", "lastActivationDate",
            "lastDeactivationDate", "owner", "maintainers", "characters",
            "characterGroups" };

    public static FilterProvider simpleFilterProvider = new SimpleFilterProvider()
        .addFilter("characterGroupFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(simpleFilter));

    public static FilterProvider fullFilterProvider = new SimpleFilterProvider()
        .addFilter("characterGroupFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(fullFilter));
}
