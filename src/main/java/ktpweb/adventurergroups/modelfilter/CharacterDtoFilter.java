package ktpweb.adventurergroups.modelfilter;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class CharacterDtoFilter
{

    private static String[] simpleFilter = { "id", "name", "description",
            "colorPrimary", "colorSecondary", "avatarFilename", "maintainer" };

    private static String[] fullFilter = { "id", "name", "description",
            "colorPrimary", "colorSecondary", "avatarFilename", "maintainer",
            "createDate", "instance", "characterGroup" };

    public static FilterProvider simpleFilterProvider = new SimpleFilterProvider()
        .addFilter("characterGroupFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(simpleFilter));

    public static FilterProvider fullFilterProvider = new SimpleFilterProvider()
        .addFilter("characterGroupFilter",
            SimpleBeanPropertyFilter.filterOutAllExcept(fullFilter));
}
