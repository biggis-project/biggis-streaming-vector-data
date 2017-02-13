package net.disy.biggis.kef.flink.feature;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

public class DuplicateFilter extends RichFlatMapFunction<Tuple2<Integer, KefFeature>, Tuple2<Integer, KefFeature>> {
	private static final long serialVersionUID = 1L;
	
	static final ValueStateDescriptor<Boolean> descriptor = new ValueStateDescriptor<>("seen", Boolean.class, false);
    private ValueState<Boolean> operatorState;

    @Override
    public void open(Configuration configuration) {
        operatorState = getRuntimeContext().getState(descriptor);
    }

	@Override
	public void flatMap(Tuple2<Integer, KefFeature> value, Collector<Tuple2<Integer, KefFeature>> out)
			throws Exception {
		if (!operatorState.value()) {
            out.collect(value);
            operatorState.update(true);
        }
	}

}
