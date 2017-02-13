package net.disy.biggis.kef.flink.charts;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

@SuppressWarnings("nls")
public class ImportFileNameAdapterTest {

  private final static String SAMPLE_FF = "charts\\ff\\2015\\kef-ff-689.json";

  @Test
  public void fallenfaenge() throws Exception {
    ImportFileNameAdapter adapter = new ImportFileNameAdapter(SAMPLE_FF);
    assertThat(adapter.getYear(), is(2015));
    assertThat(adapter.getIdentifier(), is(689));
    assertThat(adapter.getSampleType(), is(ImportFileNameAdapter.SampleType.CHART));
  }
}
