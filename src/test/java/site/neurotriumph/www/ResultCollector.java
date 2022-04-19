package site.neurotriumph.www;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

class ResultCollector<T> implements Answer<T> {
  private T result;

  public T getResult() {
    return result;
  }

  @Override
  public T answer(InvocationOnMock invocationOnMock) throws Throwable {
    result = (T) invocationOnMock.callRealMethod();
    return result;
  }
}