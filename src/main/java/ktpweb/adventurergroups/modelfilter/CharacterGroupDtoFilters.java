package ktpweb.adventurergroups.modelfilter;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class CharacterGroupDtoFilters
{

    protected static String[] simpleFilter = { "id", "name" };

    protected static String[] fullFilter = { "id", "name", "description",
            "colorPrimary", "createDate", "instance", "characters" };

    public static FilterProvider simpleFilterProvider = new SimpleFilterProvider()
        .addFilter("characterGroupFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(simpleFilter));

    public static FilterProvider fullFilterProvider = new SimpleFilterProvider()
        .addFilter("characterGroupFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(fullFilter))
        .addFilter("instanceFilter",
            SimpleBeanPropertyFilter
                .filterOutAllExcept(InstanceDtoFilters.simpleFilter))
        .addFilter("characterFilter", SimpleBeanPropertyFilter
            .filterOutAllExcept(CharacterDtoFilters.simpleFilter));
}
