-- Starter question bank for local development and test databases.

INSERT INTO topics (name)
VALUES
    ('Arrays'),
    ('Binary Search'),
    ('Hash Table'),
    ('Sorting'),
    ('Stack'),
    ('Strings'),
    ('Sliding Window')
ON CONFLICT (name) DO NOTHING;

INSERT INTO problems (
    title, slug, description, difficulty, status, problem_constraints,
    created_at, updated_at
)
VALUES
    (
        'Two Sum',
        'two-sum',
        'Given an integer array nums and an integer target, return the indices of two distinct elements whose values add up to target. You may assume exactly one solution exists.',
        'EASY', 'PUBLISHED',
        '2 <= nums.length <= 10000; each input has exactly one solution.',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    ),
    (
        'Binary Search',
        'binary-search',
        'Given a sorted array of distinct integers and a target, return the index of the target. Return -1 if the target is not present.',
        'EASY', 'PUBLISHED',
        '0 <= nums.length <= 100000; nums is sorted in strictly increasing order.',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    ),
    (
        'Valid Parentheses',
        'valid-parentheses',
        'Given a string containing only the characters (, ), {, }, [ and ], determine whether every opening bracket is closed by the same type of bracket in the correct order.',
        'MEDIUM', 'PUBLISHED',
        '0 <= s.length <= 10000.',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    ),
    (
        'Merge Intervals',
        'merge-intervals',
        'Given a list of intervals, merge all overlapping intervals and return the resulting list of non-overlapping intervals.',
        'MEDIUM', 'PUBLISHED',
        '1 <= intervals.length <= 10000; intervals[i] = [start, end].',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    ),
    (
        'Longest Substring Without Repeating Characters',
        'longest-substring-without-repeating-characters',
        'Given a string s, return the length of the longest substring that contains no repeated characters.',
        'MEDIUM', 'PUBLISHED',
        '0 <= s.length <= 50000.',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    )
ON CONFLICT (slug) DO NOTHING;

INSERT INTO problem_topics (problem_id, topic_id)
SELECT p.id, t.id
FROM (VALUES
    ('two-sum', 'Arrays'),
    ('two-sum', 'Hash Table'),
    ('binary-search', 'Arrays'),
    ('binary-search', 'Binary Search'),
    ('valid-parentheses', 'Stack'),
    ('valid-parentheses', 'Strings'),
    ('merge-intervals', 'Arrays'),
    ('merge-intervals', 'Sorting'),
    ('longest-substring-without-repeating-characters', 'Sliding Window'),
    ('longest-substring-without-repeating-characters', 'Strings')
) AS seed(slug, topic_name)
JOIN problems p ON p.slug = seed.slug
JOIN topics t ON t.name = seed.topic_name
ON CONFLICT (problem_id, topic_id) DO NOTHING;

INSERT INTO test_cases (problem_id, input, expected_output, hidden, sequence_order)
SELECT p.id, seed.input, seed.expected_output, seed.hidden, seed.sequence_order
FROM (VALUES
    ('two-sum', 'nums=[2,7,11,15], target=9', '[0,1]', FALSE, 1),
    ('two-sum', 'nums=[3,2,4], target=6', '[1,2]', FALSE, 2),
    ('two-sum', 'nums=[-3,4,3,90], target=0', '[0,2]', TRUE, 3),
    ('binary-search', 'nums=[-1,0,3,5,9,12], target=9', '4', FALSE, 1),
    ('binary-search', 'nums=[-1,0,3,5,9,12], target=2', '-1', FALSE, 2),
    ('binary-search', 'nums=[5], target=5', '0', TRUE, 3),
    ('valid-parentheses', 's="()[]{}"', 'true', FALSE, 1),
    ('valid-parentheses', 's="(]"', 'false', FALSE, 2),
    ('valid-parentheses', 's="{[]}"', 'true', TRUE, 3),
    ('merge-intervals', 'intervals=[[1,3],[2,6],[8,10],[15,18]]', '[[1,6],[8,10],[15,18]]', FALSE, 1),
    ('merge-intervals', 'intervals=[[1,4],[4,5]]', '[[1,5]]', FALSE, 2),
    ('merge-intervals', 'intervals=[[1,10],[2,3],[4,8]]', '[[1,10]]', TRUE, 3),
    ('longest-substring-without-repeating-characters', 's="abcabcbb"', '3', FALSE, 1),
    ('longest-substring-without-repeating-characters', 's="bbbbb"', '1', FALSE, 2),
    ('longest-substring-without-repeating-characters', 's="pwwkew"', '3', TRUE, 3)
) AS seed(slug, input, expected_output, hidden, sequence_order)
JOIN problems p ON p.slug = seed.slug
ON CONFLICT (problem_id, sequence_order) DO NOTHING;
