import time
import math
import json
import os
import re

measure_index_file = '../result/MeasureIndex.csv'
clone_group_file = '../result/type123_method_group_result.csv'
token_data_folder = '../tokenData'
state_file = '../result/state.json'
output_file = './similarity.csv'
start_offset = -2147483647
comment_pattern1 = re.compile('//.*?\n', re.S)
comment_pattern2 = re.compile('/\*.*?\*/', re.S)
space_pattern = re.compile('\s', re.S)

def init_measure_dict():
    """初始化方法列表"""
    global token_data_folder
    measure_info = dict()
    measure_list_files = list()
    for root, dirs, files in os.walk(token_data_folder):
        for file in files:
            if file.startswith('MeasureList'):
                measure_list_files.append(os.path.join(root, file))
    measure_list_files.sort()
    idx = 0
    for file in measure_list_files:
        f = open(file, 'r')
        for line in f:
            tmp = line.strip().split(',')
            measure = dict()
            measure['start_token'] = int(tmp[2])
            measure['end_token'] = int(tmp[3])
            measure_info[str(idx)] = measure
            idx += 1
        f.close()
    return measure_info

def init_measure_indecies():
    """初始化MeasureIndex.csv里的数据"""
    measure_indecies = dict()
    f = open(measure_index_file, 'r')
    for line in f:
        tmp = line.strip().split(',')
        mid = tmp[0]
        measure_indecies[mid] = tmp[1:]
    f.close()
    return measure_indecies

def read_lines(file, start_line, end_line):
    """读取文件指定行"""
    lines = list()
    try:
        f = open(file, 'r', encoding='utf8')
        lines = f.readlines()
        lines = lines[start_line-1: end_line]
    except BaseException as e:
        pass
    return lines

def remove_comments(code):
    """移除java,c,c++代码里的注释，移除代码内的空格"""
    tmp = comment_pattern1.sub('', code)
    tmp = comment_pattern2.sub('', tmp)
    tmp = space_pattern.sub('', tmp)
    return tmp

def get_code_type(code1, code2):
    """获取代码克隆类型"""
    return '1' if code1 == code2 else '2'

def init_clone_groups():
    """初始化克隆组数据"""
    global clone_group_file
    clone_groups = list()
    f = open(clone_group_file, 'r')
    for line in f:
        ids = line.strip().split(',')
        clone_groups.append(ids)
    f.close()
    return clone_groups

def init_state():
    """初始化状态数据"""
    global state_file
    with open(state_file, 'r') as f:
        state = json.load(f)
    return state

def suffix_array_similarity(tokens1, tokens2):
    """用后缀数组计算相似度"""
    measure1 = Measure(0, len(tokens1)-1)
    measure2 = Measure(len(tokens1), len(tokens1) + len(tokens2)-1)
    measures = [measure1, measure2]

    tokens = list()
    tokens.extend(tokens1)
    tokens.extend(tokens2)
    sa = SuffixArray(tokens)
    res = sa.process()
    
    clonePairs = list()
    for pair in res:
        first_from = search_index(measures, pair[0], pair[2])
        first_to = search_index(measures, pair[0] + pair[2], pair[2])
        second_from = search_index(measures, pair[1], pair[2])
        second_to = search_index(measures, pair[1] + pair[2], pair[2])
        if first_from == second_from:
            continue
        if first_from != first_to or second_from != second_to:
            continue
        if pair[2] == 0:
            continue
        x1 = min(pair[0], pair[1])
        x2 = max(pair[0], pair[1])
        clonePairs.append(ClonePair(x1, x2, pair[2]))

    #计算方法内连续片段的长度
    cover = calc_cover_length(clonePairs)
    return cover/ max(len(tokens1), len(tokens2))

def search_index(measures, pos, height):
    """搜索子串所在的方法索引"""
    idx = -1
    for i in range(len(measures)):
        if pos >= measures[i].start and (pos + height - 1) <= measures[i].end:
            idx = i
            break
    return idx

def calc_cover_length(pairs):
    """计算重叠片段长度"""
    pairs = sorted(pairs, key=lambda x:x.first)
    idx = 0
    total_size = 0
    start_token = 0
    size = 0
    while idx < len(pairs):
        if idx == 0:
            start_token = pairs[idx].first
            size = pairs[idx].size
            idx += 1
            continue
        if start_token + size >= pairs[idx].first:
            if start_token + size >= pairs[idx].first + pairs[idx].size:
                pass
            else:
                size = pairs[idx].first - start_token + pairs[idx].size
            idx += 1
        else:
            total_size += size
            start_token = pairs[idx].first
            size = pairs[idx].size
            idx += 1
    return max(size, total_size)

def process():
    """计算每个克隆组中每个克隆实例之间的相似度"""
    global token_data_folder, start_offset, measure_index_file
    measures = init_measure_dict()
    measure_indecies = init_measure_indecies()
    clone_groups = init_clone_groups()
    state = init_state()
    f = open(output_file, 'w')
    size = len(clone_groups)
    cnt = 0
    for group in clone_groups:
        cnt += 1
        print('%.2f%%' % (cnt*100.0/size))
        for i in range(0, len(group) - 1):
            for j in range(i + 1, len(group)):
                measure1 = measures[group[i]]
                measure2 = measures[group[j]]
                measure1_file_id, offset1 = calc_file_id(state, measure1['start_token'])
                measure2_file_id, offset2 = calc_file_id(state, measure2['start_token'])
                measure1_file = '%s/allTokenCsv%d' % (token_data_folder, measure1_file_id)
                measure2_file = '%s/allTokenCsv%d' % (token_data_folder, measure2_file_id)
                tokens1 = read_tokens(measure1_file, offset1, measure1['end_token'] - measure1['start_token'])
                tokens2 = read_tokens(measure2_file, offset2, measure2['end_token'] - measure2['start_token'])
                similarity = suffix_array_similarity(tokens1, tokens2)
                if 1 != int(similarity):
                    f.write('%s,%s,%f,%d\n' % (group[i], group[j], similarity, 3))
                else:
                    m1 = measure_indecies[group[i]]
                    m2 = measure_indecies[group[j]]
                    lines1 = read_lines(m1[0], int(m1[1]), int(m1[2]))
                    lines2 = read_lines(m2[0], int(m2[1]), int(m2[2]))
                    if len(lines1) == 0 or len(lines2) == 0:
                        f.write('%s,%s,%f,%d\n' % (group[i], group[j], similarity, 2))
                        continue
                    code1 = remove_comments('\n'.join(lines1))
                    code2 = remove_comments('\n'.join(lines2))
                    code_type = get_code_type(code1, code2)
                    f.write('%s,%s,%f,%s\n' % (group[i], group[j], similarity, code_type))
                
        f.write('\n')
    f.close()

def calc_file_id(state, start_token):
    """计算方法的token所在的文件以及偏移"""
    global start_offset
    token_file_num = len(state['tokenIndexList'])
    if token_file_num == 1:
        return (0, start_token - start_offset)
    idx = -1
    offset = -1
    for i in range(token_file_num - 1):
        current_offset = state['tokenIndexList'][i]
        next_offset = state['tokenIndexList'][i+1]
        if start_token >= current_offset and start_token < next_offset:
            idx = i
            offset = start_token - current_offset
            break
    idx = token_file_num - 1 if idx == -1 else idx
    offset = start_token - state['tokenIndexList'][-1] if offset == -1 else offset
    return (idx, offset)

class SuffixArray():
    def __init__(self, tokens):
        self.tokens = tokens
        self.sa = list()
        self.height = list()
    
    def _build_sa(self):
        all_suffixes = list()
        for i in range(len(self.tokens)):
            all_suffixes.append(self.tokens[i:])
        all_suffixes.sort()
        for i in range(len(all_suffixes)):
            self.sa.append(len(self.tokens) - len(all_suffixes[i]))

    def _calc_height(self):
        self.height.append(0)
        for i in range(1, len(self.tokens)):
            s1 = self.tokens[self.sa[i-1]:]
            s2 = self.tokens[self.sa[i]:]
            h = 0
            size = min(len(s1), len(s2))
            for i in range(size):
                if s1[i] != s2[i]:
                    break
                h += 1
            self.height.append(h)

    def process(self):
        self._build_sa()
        self._calc_height()
        res = list()
        for i in range(1, len(self.height)):
            res.append((self.sa[i-1], self.sa[i], self.height[i]))
        return res

class Measure():
    def __init__(self, start, end):
        self.start = start
        self.end = end

class ClonePair():
    def __init__(self, first, second, size):
        self.first = first
        self.second = second
        self.size = size


def read_tokens(file, offset, size):
    """读取保存在磁盘上的方法的token"""
    f = open(file, 'rb')
    f.seek(offset)
    content = f.read(size)
    f.close()
    tokens = list()
    for token in content:
        tokens.append(token)
    return tokens

if __name__ == '__main__':
    process()