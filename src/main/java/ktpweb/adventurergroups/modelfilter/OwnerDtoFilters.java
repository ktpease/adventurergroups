package ktpweb.adventurergroups.modelfilter;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class OwnerDtoFilters
{

    protected static String[] simpleFilter = { "id", "username",
            "displayname" };

    protected static String[] fullFilter = { "id", "username", "displayname",
            "email", "createDate", "instances" };

    public static FilterProvider simpleFilterProvider = new SimpleFilterProvider()
        .addFilter("ownerFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(simpleFilter));

    public static FilterProvider fullFilterProvider = new SimpleFilterProvider()
        .addFilter("ownerFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(fullFilter))
        .addFilter("instanceFilter", SimpleBeanPropertyFilter
            .filterOutAllExcept(InstanceDtoFilters.simpleFilter));
}
