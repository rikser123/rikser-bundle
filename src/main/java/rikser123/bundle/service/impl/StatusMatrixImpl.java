package rikser123.bundle.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import rikser123.bundle.service.StatusMatrix;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

@Service
public class StatusMatrixImpl<T extends Enum<T>> implements StatusMatrix<T> {
  private final Map<T, EnumSet<T>> transitions = new HashMap<>();

  @Override
  public void addTransition(T src, EnumSet<T> dest) {
    this.transitions.merge(src, dest, (destStatuses, newDestStatuses) -> {
      destStatuses.addAll(newDestStatuses);
      return destStatuses;
    });
  }

  @Override
  public boolean isAvailable(T src, T dest) {
    var set = transitions.get(src);

    if (CollectionUtils.isNotEmpty(set)) {
      return set.contains(dest);
    }
    return false;
  }
}
