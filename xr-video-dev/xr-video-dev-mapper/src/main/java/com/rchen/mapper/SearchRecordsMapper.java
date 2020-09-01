package com.rchen.mapper;

import com.rchen.pojo.SearchRecords;
import com.rchen.utils.MyMapper;

import java.util.List;

public interface SearchRecordsMapper extends MyMapper<SearchRecords> {

    List<String> getHotWords();
}