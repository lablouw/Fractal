package fractal.common.paremetermappers;

import fractal.common.Complex;
import java.util.Arrays;
import java.util.List;

public interface ParameterMapper {
    
    final List<ParameterMapper> availableParameterMappers = Arrays.asList(
            new StraightParameterMapper(),
            new InverseParameterMapper()
    );

	Complex map(Complex c);
    
    String getName();
    
}
