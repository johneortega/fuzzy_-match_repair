#!/bin/bash  
# script to run entire pipeline and compare to tm
# call like ./run_experiments.sh en_es 80 esplagomis where 80 is the fuzzy-match threshold
# or call like ./run_experiments.sh en_es 80 esplagomis dev where 80 is the fuzzy-match threshold where dev is
# dev or test for the set
# added a new fake on 01/07, filtering on training, no-filtering on test 
# call like ./run_experiments.sh en_es 80 esplagomis test switchtest where 80 is the fuzzy-match threshold
# dev or test for the set
alter=""
#if [ -z "$1" ]; then
#	alter="en_es"
#else
#	alter="$1"
#fi
if [ -z "$3" ]; then
	alter="esplagomis"
else
	alter="$3"
fi
runset=""
if [ -z "$4" ]; then
	runset="dev"
else
	runset="$4"
fi
if [ -z "$5" ]; then
	switchtest=""
else
	switchtest="switchtest"
fi

echo "Running the __${alter}__ training set with ${2} fms and using __${runset}__ for testing"
echo "The switchtest flag is: __${switchtest}___"

## uncomment if you are loading files
## you will need to change john.cfg also to loadfile

#if [ "${alter}" = "esplagomis" ] && [ "${switchtest}" = "" ]; then
#    if [ "${1}" = "en_es" ]; then
#        echo "Copying model_file_${1}/model_file_${2}fms_etr_nofs.${alter}.pkl model_file.pkl"
#        cp model_file_${1}/model_file_${2}fms_etr_nofs.${alter}.pkl model_file.pkl
#    else
#        echo "Copying model_file_${1}/model_file_${2}fms_etr_nofs.${alter}_*.pkl model_file.pkl"
#        cp model_file_${1}/model_file_${2}fms_etr_nofs.${alter}_*.pkl model_file.pkl
#    fi
#elif [ "${switchtest}" = "switchtest" ]; then
#    if [ "${1}" = "en_es" ]; then
#        echo "Copying switchtest model_file_${1}/model_file_${2}fms_etr_nofs.${alter}.pkl model_file.pkl"
#        cp model_file_${1}/model_file_${2}fms_etr_nofs.${alter}.pkl model_file.pkl
#        cp model_file_${1}/model_file_${2}fms_etr_nofs.${alter}.pkl model_file.pkl
#    else
#        echo "Copying model_file_${1}/model_file_${2}fms_etr_nofs.${alter}_*.pkl model_file.pkl"
#        cp model_file_${1}/model_file_${2}fms_etr_nofs.${alter}_*.pkl model_file.pkl
#    fi
#
#else
#    if [ "${1}" = "en_es" ]; then
#        echo "Copying model_file_${1}/model_file_${2}fms_etr_nofs.pkl model_file.pkl"
#        cp model_file_${1}/model_file_${2}fms_etr_nofs.pkl model_file.pkl
#    else
#        echo "Copying model_file_${1}/model_file_${2}fms_etr_nofs.wer_*.pkl model_file.pkl"
#        cp model_file_${1}/model_file_${2}fms_etr_nofs.wer_*.pkl model_file.pkl
#    fi
#fi
#
        
if [ "${alter}" = "esplagomis" ]; then
    cp model_file_es_fr_es_pt/model_file_${2}fms_etr_nofs.esplagomis*.pkl model_file.pkl
    #cp model_file_${1}/model_file_60fms_etr_nofs.esplagomis.pkl model_file.pkl
else
    cp model_file_es_fr_es_pt/model_file_${2}fms_etr_nofs.none*.pkl model_file.pkl
    #cp model_file_${1}/model_file_60fms_etr_nofs.pkl model_file.pkl
fi

if [ "${alter}" = "esplagomis" ] && [ "${switchtest}" = "" ]; then
    gzip run_global_scenario_combined_60.train.bbandbothgrounding.wer.esplagomis
	gzip run_global_scenario_${1}_${2}.test.bbandbothgrounding.wer.esplagomis
	gzip run_global_scenario_${1}_${2}.dev.bbandbothgrounding.wer.esplagomis
    ## uncomment for a train on 60% fms every time
	#./prepare_for_quest.sh run_global_scenario_${1}_60.train.bbandbothgrounding.wer.esplagomis.gz run_global_scenario_${1}_${2}.${runset}.bbandbothgrounding.wer.esplagomis.gz > prepare.out 2>&1
	./prepare_for_quest.sh run_global_scenario_combined_60.train.bbandbothgrounding.wer.esplagomis.gz run_global_scenario_${1}_${2}.${runset}.bbandbothgrounding.wer.esplagomis.gz > prepare.out 2>&1
	sleep 10
	awk -F'\t' {'print $33'} run_global_scenario_${1}_${2}.${runset}.bbandbothgrounding.wer.esplagomis | sort | uniq > grep1.o 
elif [ "${switchtest}" = "switchtest" ]; then
    echo "trying switchtest"
    sleep 100
    gzip run_global_scenario_combined_60.train.bbandbothgrounding.wer.esplagomis
	gzip run_global_scenario_${1}_${2}.dev.bbandbothgrounding.wer.esplagomis
	gzip run_global_scenario_${1}_${2}.test.bbandbothgrounding.wer.esplagomis
    ## uncomment for a train on 60% fms every time
	#./prepare_for_quest.sh run_global_scenario_${1}_60.train.bbandbothgrounding.wer.esplagomis.gz run_global_scenario_${1}_${2}.${runset}.bbandbothgrounding.wer.gz > prepare.out 2>&1
	./prepare_for_quest.sh run_global_scenario_combined_60.train.bbandbothgrounding.wer.esplagomis.gz run_global_scenario_${1}_${2}.${runset}.bbandbothgrounding.wer.gz > prepare.out 2>&1
	sleep 10
	awk -F'\t' {'print $33'} run_global_scenario_${1}_${2}.${runset}.bbandbothgrounding.wer | sort | uniq > grep1.o 
else
    gzip run_global_scenario_combined_60.train.bbandbothgrounding.wer
	gzip run_global_scenario_${1}_${2}.dev.bbandbothgrounding.wer
	gzip run_global_scenario_${1}_${2}.test.bbandbothgrounding.wer
    ## uncomment for a train on 60% fms every time
	#./prepare_for_quest.sh run_global_scenario_${1}_60.train.bbandbothgrounding.wer.gz run_global_scenario_${1}_${2}.${runset}.bbandbothgrounding.wer.gz > prepare.out 2>&1
	./prepare_for_quest.sh run_global_scenario_combined_60.train.bbandbothgrounding.wer.gz run_global_scenario_${1}_${2}.${runset}.bbandbothgrounding.wer.gz > prepare.out 2>&1

	sleep 10
	awk -F'\t' {'print $33'} run_global_scenario_${1}_${2}.${runset}.bbandbothgrounding.wer | sort | uniq > grep1.o 
fi

# print scores from final grep of tm
python find_sent_no_patches_compare.py grep1.o run_global_scenario_${1}_tm_${2}t.${runset}.out  > newawk.out 
numer=$(awk '{ sum+=$1} END {print sum}' newawk.out)
denom=$(awk '{ sum+=$2} END {print sum}' newawk.out)
#echo "TM WER:"
#echo "$numer/$denom" | bc -l
tmwer=$(echo "$numer/$denom" | bc -l)
tmedits=$numer
tmtotal=$denom

orawershow=$(grep 'wer for: parse.out.final.oracle is:' prepare.out)
orawershow=$(echo "${orawershow/wer for: parse.out.final.oracle is:/}")
orawershow="$(echo -e "${orawershow}" | tr -d '[:space:]')"

oraedshow=$(grep 'edits for: parse.out.final.oracle is:' prepare.out)
oraedshow=$(echo "${oraedshow/edits for: parse.out.final.oracle is:/}")
oraedshow="$(echo -e "${oraedshow}" | tr -d '[:space:]')"

oratotshow=$(grep 'total for: parse.out.final.oracle is:' prepare.out)
oratotshow=$(echo "${oratotshow/total for: parse.out.final.oracle is:/}")
oratotshow="$(echo -e "${oratotshow}" | tr -d '[:space:]')"

orawer=$orawershow
oraedits=$oraedshow
oratotal=$oratotshow

fmrwershow=$(grep 'wer for: parse.out.final.predicted is:' prepare.out)
fmrwershow=$(echo "${fmrwershow/wer for: parse.out.final.predicted is:/}")
fmrwershow="$(echo -e "${fmrwershow}" | tr -d '[:space:]')"

fmredshow=$(grep 'edits for: parse.out.final.predicted is:' prepare.out)
fmredshow=$(echo "${fmredshow/edits for: parse.out.final.predicted is:/}")
fmredshow="$(echo -e "${fmredshow}" | tr -d '[:space:]')"

fmrtotshow=$(grep 'total for: parse.out.final.predicted is:' prepare.out)
fmrtotshow=$(echo "${fmrtotshow/total for: parse.out.final.predicted is:/}")
fmrtotshow="$(echo -e "${fmrtotshow}" | tr -d '[:space:]')"

fmrwer=$fmrwershow
fmredits=$fmredshow
fmrtotal=$fmrtotshow

#valshow=$(grep 'score for: parse.out.final.predicted is:' prepare.out)
#valshow=$(echo "${valshow/score for: parse.out.final.predicted is:/}")
#valshow="$(echo -e "${valshow}" | tr -d '[:space:]')"

#model_file="model_file_${2}fms_etr_nofs.${alter}_${orashow}_${valshow}_${anval}.pkl"
#model_file="model_file_${2}fms_etr_nofs.${alter}_${orashow}_${valshow}_${numer}_${denom}.pkl"

#model_file="model_file_${2}fms_etr_nofs.${alter}_${orashow}_${valshow}_${numer}_${denom}.pkl"
#echo "saving modelfile: ${model_file}"
#mv model_file.pkl $model_file

#$tmwer
#$tmedits
#$tmtotal
#$fmrwer
#$fmredits
#$fmrtotal
#$orawer
#$oraedits
#$oratotal
snum=$(echo "${tmwer} - ${fmrwer}" | bc -l)
sdenom=$(echo "$tmwer - $orawer" | bc -l)
successrate=$(echo "$snum/$sdenom" | bc -l)
successrate=$(python rounder.py $successrate 1.0)
tmwer=$(python rounder.py $tmwer 100.0)
fmrwer=$(python rounder.py $fmrwer 100.0)
orawer=$(python rounder.py $orawer 100.0)

echo "============ EDIT TABLE ============="
echo "===== FMS: ${2} ====================="
echo "===== FILTER: ${alter} EVAL SET: ${runset} ======"
echo "===== SWITCH: ${switchtest} ======"
echo "===================================="
## echo out what's needed for latex on edits only
echo "\\multicolumn{1}{r|}{\$${tmwer}\$}  & "
echo "\\multicolumn{1}{r|}{\$${fmrwer}\$}  & "
echo "\\multicolumn{1}{r|}{\$${orawer}\$}  & "
echo "\\multicolumn{1}{r|}{\$${successrate}\$}   & "

echo "============ EDIT TABLE ============="
echo "===== FMS: ${2} ====================="
echo "===== FILTER: ${alter} EVAL SET: ${runset} ======"
echo "===== SWITCH: ${switchtest} ======"
echo "===================================="
## echo out what's needed for latex on edits only
echo "\\multicolumn{1}{r|}{\$${tmedits},${tmtotal}\$}  & "
echo "\\multicolumn{1}{r|}{\$${fmredits},${fmrtotal}\$}  & "
echo "\\multicolumn{1}{r|}{\$${oraedits},${oratotal}\$}  & "
echo "\\multicolumn{1}{r|}{\$${successrate}\$}   & "

model_file="model_file_${2}fms_etr_nofs.${alter}_${tmwer}_${fmrwer}_${orawer}_${successrate}.pkl"
#model_file="model_file_${2}fms_svr.${alter}_${tmwer}_${fmrwer}_${orawer}_${successrate}.pkl"
#echo "saving modelfile: ${model_file}"
#mv model_file.pkl $model_file
