#include "m_pd.h"
//sleep thread
#include <unistd.h> // for usleep
//amp logics
#include <string.h>
#include <math.h>
#include <stdlib.h>

static t_class *counter_class;
int gen_time = 0;


//-------------------trans amp
short dataToSend[]
          // = { 7, 7, 19, 7, 7, 7  }; // highest number should be 2^13-1 =
          // 4095
          = { 1, 17, 33, 49, 65, 81 };
      // = { 1, 1, 1, 1, 1, 1};
      // = { 1, 7, 5, 4, 19, 1 }; // highest number should be 2^13-1 = 4095
      // = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4095 }; //
      // highest number should be 2^13-1 = 4095

int const size = 16;
int i;
int iter;
int number[16];

//-------------------end trans amp

typedef struct _counter {
  t_object  x_obj;
  int i_count, i_init;
  t_outlet *out_num, *out_sync, *out_fq1, *out_fq2, *out_fq3, *out_fq4, *out_fq5, *out_fq6, *out_fq7, *out_fq8, *out_fq9, *out_fq10, *out_fq11, *out_fq12, *out_fq13;
} t_counter;

//Reset the count to start at 0
void counter_resetCount(t_counter *x){
    //Initialize the counts to start at 0 again
    x->i_count = x->i_init;
}

void counter_bang(t_counter *x)
{
  t_float f=x->i_count;
  x->i_count++;
  outlet_float(x->out_num, f);
  if ((int)f % 10  == 0)
    outlet_bang(x->out_sync);
}

void counter_onResetMsg(t_counter *x){
  counter_resetCount(x);

  int milliseconds = 150;
  usleep(milliseconds * 1000);
  post("Its time %d",gen_time);
  gen_time++;
}

int* generateMatrix (char *msg)
{ // Generates a Hamming code according to the number of data bits
  int r = 0, m = strlen(msg);
  // calculate number of parity bits needed using m+r+1<=2^r
  while (1)
    {
      if (m + r + 1 <= pow (2, r))
        {
          break;
        }
      r++;
    }
  // System.out.println("Number of parity bits needed : "+r);
  int transLength = strlen(msg) + r, temp = 0, temp2 = 0, j = 0;
  int transMsg[transLength + 1]; //+1 because starts with 1
  for (i = 1; i <= transLength; i++)
  {
    temp2 = (int) pow (2, temp);
    if (i % temp2 != 0)
      {
        char theval[2]="";
        strncpy(theval,&msg[j],1);
        transMsg[i] = atoi (theval);
        j++;
      }
    else
      {
        temp++;
      }
  }

  // for(int i=1;i<=transLength;i++)
  // {
  // 	System.out.print(transMsg[i]);
  // }
  // System.out.println();
  
  for (i = 0; i < r; i++)
    {
      int smallStep = (int) pow (2, i);
      int bigStep = smallStep * 2;
      int start = smallStep, checkPos = start;
      // System.out.println("Calculating Parity bit for Position :
      // "+smallStep); System.out.print("Bits to be checked : ");
      while (1)
        {
          for (int k = start; k <= start + smallStep - 1; k++)
            {
              checkPos = k;
              // System.out.print(checkPos+" ");
              if (k > transLength)
                {
                  break;
                }
              transMsg[smallStep] = ((int) (transMsg[smallStep])) ^ ((int) (transMsg[checkPos]));
            }
          if (checkPos > transLength)
            {
              break;
            }
          else
            {
              start = start + bigStep;
            }
        }
      // System.out.println();
    }
  // Display encoded message
  printf ("Hamming Encoded Message : ");
  int v;
  for (v = 1; v <= transLength; v++)
  {
    printf ("%d ",transMsg[v]);
  }
  printf("\n");

  int *matrix;
  matrix  = (int*) malloc((sizeof(int)*transLength));

  
  for(v = 0; v < transLength; v++)
  {
    matrix[v] = 0;
  }
  
  for (v = 1; v <= transLength; v++)
    {
      matrix[v - 1] = transMsg[v];
      //  System.arraycopy(transMsg, (i*transLength), matrix[i], 0,
      //  transLength);
    }
  // System.out.println("\n\n"+Integer.toString(matrix.length)+" Here:
  // "+Arrays.toString(matrix));
  return matrix;
}

int hammingBinToI (int * arr){
  int v;
  int sum = 0;
  for (v=0; v < 15; v++){
    sum += pow(2,v) * arr[v]; 
  }
  return sum;
}

void
bin (unsigned short v, int numbers[], int Size)
{
  for (; Size - 1 >= 0; Size--)
    numbers[Size - 1] = (v >> (Size - 1)) & 1;
}

// void counter_onAmpMsg(t_counter *x){
//   char strTransmission[17]="";  // String with the data to generate a hamming code
//   char strMatrix[13] = "";
//   int * transmission;


//   for (i = 0; i < 3; i++)
//   {
//     if(i != 1){
//       // player13.setVolume ("0.5");
//       outlet_float(x->out_fq13, (t_float) 0.5);
//     }
//     else{
//       // player13.setVolume ("0.1");
//       outlet_float(x->out_fq13, (t_float) 0.1);
//     }
//     usleep(150 * 1000);
//   }

//   for (int t = 0; t < size; t++)
//   {
//     number[t] = 0;
//   }

//   unsigned int u;
//   for (u = 0; u < sizeof (dataToSend) / sizeof (dataToSend[0]); u++)
//   {
//     printf ("number: %d\n", dataToSend[u]);
//     bin (dataToSend[u], number, size);

//     for (int t = 0; t < size; t++)
//     {
//       printf ("%d", number[t]);
//     }

//     printf ("\n");

//     for (int k = 0; k < size; k++)
//     { 
//       char c[2];
//       sprintf(c, "%d", number[k]);
//       if (!(number[k] == 0 && strlen(strTransmission) == 0))
//         strcat(strTransmission, c);
//     }
   
//     strncpy(strMatrix, strTransmission, 12);
//     transmission = generateMatrix(strMatrix);
//     printf("Valor: %d\n", hammingBinToI(transmission));
//     fflush(stdout);

//     bin ((unsigned short) hammingBinToI(transmission), number, size);

//     printf ("Hamming Num:");          
//     for (int t = 0; t < 12; t++)
//       {
//         printf ("%d", number[t]);
//       }

//     printf ("\n\n");

//     if (number[0])
//       // player1.setVolume ("1");
//       outlet_float(x->out_fq1, (t_float) 1);
//     else
//       // player1.setVolume ("0.3");
//       outlet_float(x->out_fq1, (t_float) 0.3);
//     if (number[1])
//       // player2.setVolume ("1");
//       outlet_float(x->out_fq2, (t_float) 1);
//     else
//       // player2.setVolume ("0.3");
//       outlet_float(x->out_fq2, (t_float) 0.3);
//     if (number[2])
//       // player3.setVolume ("1");
//       outlet_float(x->out_fq3, (t_float) 1);
//     else
//       // player3.setVolume ("0.3");
//       outlet_float(x->out_fq3, (t_float) 0.3);
//     if (number[3])
//       // player4.setVolume ("1");
//       outlet_float(x->out_fq4, (t_float) 1);
//     else
//       // player4.setVolume ("0.3");
//       outlet_float(x->out_fq4, (t_float) 0.3);
//     if (number[4])
//       // player5.setVolume ("1");
//       outlet_float(x->out_fq5, (t_float) 1);
//     else
//       // player5.setVolume ("0.3");
//       outlet_float(x->out_fq5, (t_float) 0.3);
//     if (number[5])
//       // player6.setVolume ("1");
//       outlet_float(x->out_fq6, (t_float) 1);
//     else
//       // player6.setVolume ("0.3");
//       outlet_float(x->out_fq6, (t_float) 0.3);
//     if (number[6])
//       // player7.setVolume ("1");
//       outlet_float(x->out_fq7, (t_float) 1);
//     else
//       // player7.setVolume ("0.3");
//       outlet_float(x->out_fq7, (t_float) 0.3);
//     if (number[7])
//       // player8.setVolume ("1");
//       outlet_float(x->out_fq8, (t_float) 1);
//     else
//       // player8.setVolume ("0.3");
//       outlet_float(x->out_fq8, (t_float) 0.3);
//     if (number[8])
//       // player9.setVolume ("1");
//       outlet_float(x->out_fq9, (t_float) 1);
//     else
//       // player9.setVolume ("0.3");
//       outlet_float(x->out_fq9, (t_float) 0.3);
//     if (number[9])
//       // player10.setVolume ("1");
//       outlet_float(x->out_fq10, (t_float) 1);
//     else
//       // player10.setVolume ("0.3");
//       outlet_float(x->out_fq10, (t_float) 0.3);
//     if (number[10])
//       // player11.setVolume ("1");
//       outlet_float(x->out_fq11, (t_float) 1);
//     else
//       // player11.setVolume ("0.3");
//       outlet_float(x->out_fq11, (t_float) 0.3);
//     if (number[11])
//       // player12.setVolume ("1");
//       outlet_float(x->out_fq12, (t_float) 1);
//     else
//       // player12.setVolume ("0.3");
//       outlet_float(x->out_fq12, (t_float) 0.3);

//     usleep(150 * 1000);

//     for (int t = 0; t < size; t++)
//       {
//         number[t] = 0;
//       }

//     // player1.setVolume ("0.3");
//     // player2.setVolume ("0.3");
//     // player3.setVolume ("0.3");
//     // player4.setVolume ("0.3");
//     // player5.setVolume ("0.3");
//     // player6.setVolume ("0.3");
//     // player7.setVolume ("0.3");
//     // player8.setVolume ("0.3");
//     // player9.setVolume ("0.3");
//     // player10.setVolume ("0.3");
//     // player11.setVolume ("0.3");
//     // player12.setVolume ("0.3");
//     outlet_float(x->out_fq1, (t_float) 0.3);
//     outlet_float(x->out_fq2, (t_float) 0.3);
//     outlet_float(x->out_fq3, (t_float) 0.3);
//     outlet_float(x->out_fq4, (t_float) 0.3);
//     outlet_float(x->out_fq5, (t_float) 0.3);
//     outlet_float(x->out_fq6, (t_float) 0.3);
//     outlet_float(x->out_fq7, (t_float) 0.3);
//     outlet_float(x->out_fq8, (t_float) 0.3);
//     outlet_float(x->out_fq9, (t_float) 0.3);
//     outlet_float(x->out_fq10, (t_float) 0.3);
//     outlet_float(x->out_fq11, (t_float) 0.3);
//     outlet_float(x->out_fq12, (t_float) 0.3);

//     usleep(300 * 1000);
//     memset(strTransmission,0,strlen(strTransmission));
//   }  
// }

void counter_onSilenceMsg (t_counter *x){
  // player1.setVolume ("0.3");
    // player2.setVolume ("0.3");
    // player3.setVolume ("0.3");
    // player4.setVolume ("0.3");
    // player5.setVolume ("0.3");
    // player6.setVolume ("0.3");
    // player7.setVolume ("0.3");
    // player8.setVolume ("0.3");
    // player9.setVolume ("0.3");
    // player10.setVolume ("0.3");
    // player11.setVolume ("0.3");
    // player12.setVolume ("0.3");
    outlet_float(x->out_fq1, (t_float) 0.3);
    outlet_float(x->out_fq2, (t_float) 0.3);
    outlet_float(x->out_fq3, (t_float) 0.3);
    outlet_float(x->out_fq4, (t_float) 0.3);
    outlet_float(x->out_fq5, (t_float) 0.3);
    outlet_float(x->out_fq6, (t_float) 0.3);
    outlet_float(x->out_fq7, (t_float) 0.3);
    outlet_float(x->out_fq8, (t_float) 0.3);
    outlet_float(x->out_fq9, (t_float) 0.3);
    outlet_float(x->out_fq10, (t_float) 0.3);
    outlet_float(x->out_fq11, (t_float) 0.3);
    outlet_float(x->out_fq12, (t_float) 0.3);
}

void counter_onHighMsg (t_counter *x){
  outlet_float(x->out_fq1, (t_float) 0.5);
  outlet_float(x->out_fq2, (t_float) 0.5);
  outlet_float(x->out_fq3, (t_float) 0.5);
  outlet_float(x->out_fq4, (t_float) 0.5);
  outlet_float(x->out_fq5, (t_float) 0.5);
  outlet_float(x->out_fq6, (t_float) 0.5);
  outlet_float(x->out_fq7, (t_float) 0.5);
  outlet_float(x->out_fq8, (t_float) 0.5);
  outlet_float(x->out_fq9, (t_float) 0.5);
  outlet_float(x->out_fq10, (t_float) 0.5);
  outlet_float(x->out_fq11, (t_float) 0.5);
  outlet_float(x->out_fq12, (t_float) 0.5);
  outlet_float(x->out_fq13, (t_float) 0.5);
}

void counter_onAmpMsg(t_counter *x){
  char strTransmission[17]="";  // String with the data to generate a hamming code
  char strMatrix[13] = "";
  int * transmission;


  if(iter == 0 || iter == 2){
    // player13.setVolume ("0.5");
    outlet_float(x->out_fq13, (t_float) 0.5);
  }
  else if (iter == 1){
    // player13.setVolume ("0.1");
    outlet_float(x->out_fq13, (t_float) 0.1);
  }
  else{
    for (int t = 0; t < size; t++)
    {
      number[t] = 0;
    }

    unsigned int u = iter-3;

    printf ("number: %d\n", dataToSend[u]);
    bin (dataToSend[u], number, size);

    for (int t = 0; t < size; t++)
    {
      printf ("%d", number[t]);
    }

    printf ("\n");

    for (int k = 0; k < size; k++)
    { 
      char c[2];
      sprintf(c, "%d", number[k]);
      if (!(number[k] == 0 && strlen(strTransmission) == 0))
        strcat(strTransmission, c);
    }
  
    strncpy(strMatrix, strTransmission, 12);
    transmission = generateMatrix(strMatrix);
    printf("Valor: %d\n", hammingBinToI(transmission));
    fflush(stdout);

    bin ((unsigned short) hammingBinToI(transmission), number, size);

    printf ("Hamming Num:");          
    for (int t = 0; t < 12; t++)
      {
        printf ("%d", number[t]);
      }

    printf ("\n\n");

    if (number[0])
      // player1.setVolume ("1");
      outlet_float(x->out_fq1, (t_float) 1);
    else
      // player1.setVolume ("0.3");
      outlet_float(x->out_fq1, (t_float) 0.3);
    if (number[1])
      // player2.setVolume ("1");
      outlet_float(x->out_fq2, (t_float) 1);
    else
      // player2.setVolume ("0.3");
      outlet_float(x->out_fq2, (t_float) 0.3);
    if (number[2])
      // player3.setVolume ("1");
      outlet_float(x->out_fq3, (t_float) 1);
    else
      // player3.setVolume ("0.3");
      outlet_float(x->out_fq3, (t_float) 0.3);
    if (number[3])
      // player4.setVolume ("1");
      outlet_float(x->out_fq4, (t_float) 1);
    else
      // player4.setVolume ("0.3");
      outlet_float(x->out_fq4, (t_float) 0.3);
    if (number[4])
      // player5.setVolume ("1");
      outlet_float(x->out_fq5, (t_float) 1);
    else
      // player5.setVolume ("0.3");
      outlet_float(x->out_fq5, (t_float) 0.3);
    if (number[5])
      // player6.setVolume ("1");
      outlet_float(x->out_fq6, (t_float) 1);
    else
      // player6.setVolume ("0.3");
      outlet_float(x->out_fq6, (t_float) 0.3);
    if (number[6])
      // player7.setVolume ("1");
      outlet_float(x->out_fq7, (t_float) 1);
    else
      // player7.setVolume ("0.3");
      outlet_float(x->out_fq7, (t_float) 0.3);
    if (number[7])
      // player8.setVolume ("1");
      outlet_float(x->out_fq8, (t_float) 1);
    else
      // player8.setVolume ("0.3");
      outlet_float(x->out_fq8, (t_float) 0.3);
    if (number[8])
      // player9.setVolume ("1");
      outlet_float(x->out_fq9, (t_float) 1);
    else
      // player9.setVolume ("0.3");
      outlet_float(x->out_fq9, (t_float) 0.3);
    if (number[9])
      // player10.setVolume ("1");
      outlet_float(x->out_fq10, (t_float) 1);
    else
      // player10.setVolume ("0.3");
      outlet_float(x->out_fq10, (t_float) 0.3);
    if (number[10])
      // player11.setVolume ("1");
      outlet_float(x->out_fq11, (t_float) 1);
    else
      // player11.setVolume ("0.3");
      outlet_float(x->out_fq11, (t_float) 0.3);
    if (number[11])
      // player12.setVolume ("1");
      outlet_float(x->out_fq12, (t_float) 1);
    else
      // player12.setVolume ("0.3");
      outlet_float(x->out_fq12, (t_float) 0.3);
    
    for (int t = 0; t < size; t++)
    {
      number[t] = 0;
    }

    // memset(strTransmission,0,strlen(strTransmission));  
  }

  iter++;
  if (iter >= 9)  // sizeof (dataToSend) / sizeof (dataToSend[0]
    iter = 0;
  printf("Valor do iter: %d\n",iter);
}

void *counter_new(t_floatarg f)
{
  t_counter *x = (t_counter *)pd_new(counter_class);

  x->i_init=f;
  x->i_count=f;
  x->out_num = outlet_new(&x->x_obj, &s_float);
  x->out_sync = outlet_new(&x->x_obj, &s_bang);
  
  x->out_fq1 = outlet_new(&x->x_obj, &s_float);
  x->out_fq2 = outlet_new(&x->x_obj, &s_float);
  x->out_fq3 = outlet_new(&x->x_obj, &s_float);
  x->out_fq4 = outlet_new(&x->x_obj, &s_float);
  x->out_fq5 = outlet_new(&x->x_obj, &s_float);
  x->out_fq6 = outlet_new(&x->x_obj, &s_float);
  x->out_fq7 = outlet_new(&x->x_obj, &s_float);
  x->out_fq8 = outlet_new(&x->x_obj, &s_float);
  x->out_fq9 = outlet_new(&x->x_obj, &s_float);
  x->out_fq10 = outlet_new(&x->x_obj, &s_float);
  x->out_fq11 = outlet_new(&x->x_obj, &s_float);
  x->out_fq12 = outlet_new(&x->x_obj, &s_float);
  x->out_fq13 = outlet_new(&x->x_obj, &s_float);

  return (void *)x;
}

void counter_free(t_counter *x){
  outlet_free(x->out_num);
  outlet_free(x->out_sync);

  outlet_free(x->out_fq1);
  outlet_free(x->out_fq2);
  outlet_free(x->out_fq3);
  outlet_free(x->out_fq4);
  outlet_free(x->out_fq5);
  outlet_free(x->out_fq6);
  outlet_free(x->out_fq7);
  outlet_free(x->out_fq8);
  outlet_free(x->out_fq9);
  outlet_free(x->out_fq10);
  outlet_free(x->out_fq11);
  outlet_free(x->out_fq12);
  outlet_free(x->out_fq13);

}

void counter_setup(void) {
  counter_class = class_new(gensym("counter"),
        (t_newmethod)counter_new,
        (t_method)counter_free,
        sizeof(t_counter),
        CLASS_DEFAULT,
        A_DEFFLOAT, 0);

  class_addbang(counter_class, counter_bang);

  //Reset: start counting at init value again
    class_addmethod(counter_class,
                    (t_method)counter_onResetMsg,
                    gensym("reset"),
                    0);
  // Get new value amp
  class_addmethod(counter_class,
                    (t_method)counter_onAmpMsg,
                    gensym("amp"),
                    0);
  // Set to low amplitude
  class_addmethod(counter_class,
                    (t_method)counter_onSilenceMsg,
                    gensym("silence"),
                    0);
  // Set to high amplitude
  class_addmethod(counter_class,
                    (t_method)counter_onHighMsg,
                    gensym("high"),
                    0);
}