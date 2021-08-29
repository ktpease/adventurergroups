package ktpweb.adventurergroups.modelfilter;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class OwnerDtoFilter
{

    private static String[] simpleFilter = { "id", "username", "displayname",
            "avatarFilename" };

    private static String[] fullFilter = { "id", "username", "displayname",
            "avatarFilename", "email", "createDate", "instances" };

    public static FilterProvider simpleFilterProvider = new SimpleFilterProvider()
        .addFilter("characterGroupFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(simpleFilter));

    public static FilterProvider fullFilterProvider = new SimpleFilterProvider()
        .addFilter("characterGroupFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(fullFilter));
}
