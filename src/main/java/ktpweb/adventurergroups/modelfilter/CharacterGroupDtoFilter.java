package ktpweb.adventurergroups.modelfilter;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class CharacterGroupDtoFilter
{

    private static String[] simpleFilter = { "id", "name" };

    private static String[] fullFilter = { "id", "name", "properties",
            "colorPrimary", "createDate", "instance", "characters" };

    public static FilterProvider simpleFilterProvider = new SimpleFilterProvider()
        .addFilter("characterGroupFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(simpleFilter));

    public static FilterProvider fullFilterProvider = new SimpleFilterProvider()
        .addFilter("characterGroupFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(fullFilter));
}
