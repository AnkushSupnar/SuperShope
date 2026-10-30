package com.ankush.data.service;

import com.ankush.data.entities.BillingCounter;
import com.ankush.data.entities.TempTransaction;
import com.ankush.data.repository.BillingCounterRepository;
import com.ankush.data.repository.TempTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Billing counters and the lines of the bill in progress on each of them (temptransaction). */
@Service
public class BillingCounterService {
    @Autowired
    BillingCounterRepository counterRepository;
    @Autowired
    TempTransactionRepository tempRepository;

    /** First start after the upgrade: create Counter 1 so billing works straight away. */
    public void ensureDefaultCounter() {
        if (counterRepository.count() == 0) {
            counterRepository.save(BillingCounter.builder().counterno(1).name("Counter 1").active(true).build());
        }
    }

    public List<BillingCounter> getAllCounters() {
        return counterRepository.findAllByOrderByCounternoAsc();
    }

    public List<BillingCounter> getActiveCounters() {
        return getAllCounters().stream().filter(BillingCounter::isActiveCounter).collect(Collectors.toList());
    }

    public BillingCounter getByCounterno(Integer counterno) {
        return counterRepository.getByCounterno(counterno);
    }

    /** Adds or updates a counter from the master screen. */
    public BillingCounter saveCounter(BillingCounter counter) {
        if (counter.getCounterno() == null || counter.getCounterno() <= 0)
            throw new IllegalArgumentException("Enter counter number");
        if (counter.getName() == null || counter.getName().isBlank())
            throw new IllegalArgumentException("Enter counter name");
        BillingCounter sameNo = counterRepository.getByCounterno(counter.getCounterno());
        if (sameNo != null && !sameNo.getId().equals(counter.getId()))
            throw new IllegalArgumentException("Counter number " + counter.getCounterno() + " already exists");
        if (counter.getId() != null) {
            BillingCounter old = counterRepository.findById(counter.getId()).orElse(null);
            if (old != null && tempRepository.countByCounterno(old.getCounterno()) > 0) {
                // the pending bill's lines are keyed by counter number
                if (!old.getCounterno().equals(counter.getCounterno()))
                    throw new IllegalArgumentException("Counter " + old.getCounterno() + " has a pending bill. Save or clear it before changing its number");
                if (!counter.isActiveCounter())
                    throw new IllegalArgumentException("Counter " + old.getCounterno() + " has a pending bill. Save or clear it before making it inactive");
            }
            if (old != null) {
                // the master screen doesn't edit the draft bill
                counter.setCustomername(old.getCustomername());
                counter.setCustomermobile(old.getCustomermobile());
                counter.setOthercharges(old.getOthercharges());
            }
        }
        counter.setName(counter.getName().trim());
        return counterRepository.save(counter);
    }

    public List<TempTransaction> getLines(Integer counterno) {
        return tempRepository.findByCounternoOrderByLinenoAsc(counterno);
    }

    public long getLineCount(Integer counterno) {
        return tempRepository.countByCounterno(counterno);
    }

    /** Replaces the counter's in-progress lines with {@code lines} in one transaction. */
    @Transactional
    public void replaceLines(Integer counterno, List<TempTransaction> lines) {
        tempRepository.deleteByCounterno(counterno);
        for (int i = 0; i < lines.size(); i++) {
            TempTransaction t = lines.get(i);
            t.setId(null);
            t.setCounterno(counterno);
            t.setLineno(i + 1);
        }
        tempRepository.saveAll(lines);
    }

    public void saveDraft(Integer counterno, String customerName, String customerMobile, Float otherCharges) {
        BillingCounter counter = counterRepository.getByCounterno(counterno);
        if (counter == null) return;
        counter.setCustomername(customerName);
        counter.setCustomermobile(customerMobile);
        counter.setOthercharges(otherCharges);
        counterRepository.save(counter);
    }

    /** Discards the bill in progress on the counter: its lines and customer details. */
    @Transactional
    public void clearCounter(Integer counterno) {
        tempRepository.deleteByCounterno(counterno);
        saveDraft(counterno, null, null, null);
    }

    /** counterno -> {line count, total amount} for counters that have a pending bill. */
    public Map<Integer, double[]> getPendingSummary() {
        Map<Integer, double[]> map = new HashMap<>();
        for (Object[] row : tempRepository.getSummary()) {
            map.put((Integer) row[0], new double[]{((Number) row[1]).doubleValue(),
                    row[2] == null ? 0 : ((Number) row[2]).doubleValue()});
        }
        return map;
    }
}
